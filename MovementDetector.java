package programmering1_te20a;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

public class MovementDetector2 {
    public static void main(String[] args) {
        // Load OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Open the camera
        VideoCapture capture = new VideoCapture(0);

        // Background subtraction model
        boolean useMOG2 = args.length > 1 ? args[1] == "MOG2" : true;
        BackgroundSubtractor backSub;
        backSub = Video.createBackgroundSubtractorMOG2();
        
        
        JFrame frame = new JFrame();
        JLabel label = new JLabel();
        frame.add(label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // Current frame
        Mat currentFrame = new Mat();

        while (true) {
            // Capture a new frame from the camera
            capture.read(currentFrame);

            // Convert current frame to grayscale
            Mat gray = new Mat();
            Imgproc.cvtColor(currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

            // Apply background subtraction
            Mat fgMask = new Mat();
            backSub.apply(gray, fgMask);

            // Apply morphological operations to remove noise
            Imgproc.erode(fgMask, fgMask, new Mat());
            Imgproc.dilate(fgMask, fgMask, new Mat());

            // Find contours in the binary image
            Mat hierarchy = new Mat();
            java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
            Imgproc.findContours(fgMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            

            // Draw bounding boxes around the contours
            for (MatOfPoint contour : contours) {
                Rect boundingBox = Imgproc.boundingRect(contour);
                Imgproc.rectangle(currentFrame, boundingBox.tl(), boundingBox.br(), new Scalar(0, 255, 0), 2);
            }

            // Display the current frame
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", currentFrame, matOfByte);
            byte[] byteArray = matOfByte.toArray();

            try {
                BufferedImage bufImage = ImageIO.read(new ByteArrayInputStream(byteArray));
                ImageIcon imageIcon = new ImageIcon(bufImage);
                label.setIcon(imageIcon);
                frame.getContentPane().removeAll();
                frame.getContentPane().add(new JLabel(imageIcon));
                frame.pack();
                frame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
