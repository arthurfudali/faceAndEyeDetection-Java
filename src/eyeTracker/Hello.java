package eyeTracker;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;

public class Hello {

    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    public static void main(String[] args) {
        // Carrega os classificadores Haar para detecção de rosto e olhos
        CascadeClassifier faceCascade = new CascadeClassifier("C:\\temp\\ws-eclipse\\eyeTracker\\src\\eyeTracker\\haarcascade_frontalface_default.xml");
        CascadeClassifier eyeCascade = new CascadeClassifier("C:\\temp\\ws-eclipse\\eyeTracker\\src\\eyeTracker\\haarcascade_eye.xml");
        CascadeClassifier eyeCascadeWithGlasses = new CascadeClassifier("C:\\temp\\ws-eclipse\\eyeTracker\\src\\eyeTracker\\haarcascade_eye_tree_eyeglasses.xml");

        // Inicializa a captura de vídeo com OpenCV
        VideoCapture cam = new VideoCapture(0); // 0 para a webcam padrão
        if (!cam.isOpened()) {
            System.out.println("Erro ao abrir a câmera.");
            return;
        }

        JFrame frame = new JFrame("Eye Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel();
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setSize(640, 480);
        frame.setVisible(true);

        Mat img = new Mat();
        while (cam.read(img)) {
            Mat gray = new Mat();
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
         // Pré-processamento: Equalização do histograma para melhorar o contraste
            Imgproc.equalizeHist(gray, gray);

            // Detecção de rostos
            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(gray, faces, 1.1, 5);

            // Para cada rosto detectado
            for (Rect face : faces.toArray()) {
                Imgproc.rectangle(img, face, new Scalar(255, 0, 0), 1); // o que está dentro do Scalar() é o que define a cor do retangulo do rosto em RGB, o 1 depois é a espessura da linha

                // Região de Interesse (ROI) para os olhos dentro do rosto detectado
                Mat faceROI = gray.submat(face);
                MatOfRect eyes = new MatOfRect();
                Size minEyeSize = new Size(15, 15); // Tamanho mínimo para detecção de olhos
                Size maxEyeSize = new Size(face.width, face.height / 2); // Define um tamanho máximo com base no rosto

                // Detecção de olhos dentro do ROI de rosto
                eyeCascade.detectMultiScale(faceROI, eyes, 1.3, 5, 0, minEyeSize, maxEyeSize);
                // Detecção de olhos com óculos dentro do ROI de rosto
                eyeCascadeWithGlasses.detectMultiScale(faceROI, eyes, 1.3, 5, 0, minEyeSize, maxEyeSize);


                //eyeCascade.detectMultiScale(faceROI, eyes, 1.3);
                for (Rect eye : eyes.toArray()) {
                    Point center = new Point(face.x + eye.x + eye.width / 2, face.y + eye.y + eye.height / 2);
                    int radius = (int) Math.round((eye.width + eye.height) * 0.1);
                    Imgproc.circle(img, center, radius, new Scalar(255, 255, 0), 1);
                }
            }

            // Converte o frame para BufferedImage e exibe na interface gráfica
            ImageIcon icon = new ImageIcon(matToBufferedImage(img));
            label.setIcon(icon);
            frame.repaint();

            // Pressione 'q' para sair
            if (frame.getKeyListeners().length == 0) {
                frame.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyPressed(java.awt.event.KeyEvent evt) {
                        if (evt.getKeyChar() == 'q') {
                            cam.release();
                            frame.dispose();
                        }
                    }
                });
            }
        }

        cam.release();
    }

    // Método para converter Mat para BufferedImage
    public static BufferedImage matToBufferedImage(Mat matrix) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            bufImage = ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }
}