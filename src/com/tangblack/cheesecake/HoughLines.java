package com.tangblack.cheesecake;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.CV_AA;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.bitwise_not;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvCreateMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvLine;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.line;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_COLOR;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_ADAPTIVE_THRESH_MEAN_C;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_GRAY2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.CV_HOUGH_MULTI_SCALE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_HOUGH_PROBABILISTIC;
import static org.bytedeco.javacpp.opencv_imgproc.CV_HOUGH_STANDARD;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.GaussianBlur;
import static org.bytedeco.javacpp.opencv_imgproc.adaptiveThreshold;
import static org.bytedeco.javacpp.opencv_imgproc.cvCanny;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvHoughLines2;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;

/**
 * C to Java translation of the houghlines.c sample provided in the c sample directory of OpenCV 2.1,
 * using the JavaCV Java wrapper of OpenCV 2.2 developped by Samuel Audet.
 *
 * @author Jeremy Nicola
 * jeremy.nicola@gmail.com
 * @see <a href="https://github.com/ronnywang/tw-campaign-finance/blob/master/scripts/pic2linesjson.cpp">pic2linesjson 改成 threshold, minLineLength, maxLineGap 可以自帶參數</a>
 */
public class HoughLines {

	/**
	 * Official example.
	 *
	 * @param arg1
	 * @param arg2
	 */
	public static void execute(String arg1, String arg2)
	{
		String[] args = new String[]{arg1, arg2};
		
		
      String fileName = args.length >= 1 ? args[0] : "pic1.png"; // if no params provided, compute the defaut image
      IplImage src = cvLoadImage(fileName, 0);
      IplImage dst;
      IplImage colorDst;
      CvMemStorage storage = cvCreateMemStorage(0);
      CvSeq lines = new CvSeq();

//      CanvasFrame source = new CanvasFrame("Source");
//      CanvasFrame hough = new CanvasFrame("Hough");
      if (src == null) {
          System.out.println("Couldn't load source image.");
          return;
      }

      dst = cvCreateImage(cvGetSize(src), src.depth(), 1);
      colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);

      
//      cvCopy(src, colorDst);
      /**
       * 圖像的邊緣檢測的原理是檢測出圖像中所有灰度值變化較大的點，而且這些點連接起來就構成了若干線條，這些線條就可以稱為圖像的邊緣。
       * 第一個參數表示輸入圖像，必須為單通道灰度圖。
       * 第二個參數表示輸出的邊緣圖像，為單通道黑白圖。
       * 第三個參數和第四個參數表示閾值，這二個閾值中當中的小閾值用來控制邊緣連接，大的閾值用來控制強邊緣的初始分割即如果一個像素的梯度大與上限值，則被認為是邊緣像素，如果小於下限閾值，則被拋棄。如果該點的梯度在兩者之間則當這個點與高於上限值的像素點連接時我們才保留，否則刪除。
       * 第五個參數表示Sobel算子大小，默認為3即表示一個3*3的矩陣。Sobel算子與高斯拉普拉斯算子都是常用的邊緣算子，詳細的數學原理可以查閱專業書籍。
       */
      cvCanny(src, dst, 50, 200, 3); // input, output, threshold1, threshold2, sobel
      cvCvtColor(dst, colorDst, CV_GRAY2BGR);
      
      
      cvSaveImage( "/Users/tangblack/Desktop/result_dst.jpg" ,dst); 
      cvSaveImage( "/Users/tangblack/Desktop/result_colorDst.jpg" ,colorDst); 

      /*
       * apply the probabilistic hough transform
       * which returns for each line deteced two points ((x1, y1); (x2,y2))
       * defining the detected segment
       */
      if (args.length == 2 && args[1].contentEquals("probabilistic")) { 
          System.out.println("Using the Probabilistic Hough Transform");
//          lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 40, 50, 10);
          /**
           * threshold: 阈值参数。如果相应的累计值大于 threshold， 则函数返回的这个线段.
           * minLineLength: 对概率 Hough 变换，它是最小线段长度.
           * maxLineGap: 对概率 Hough 变换，这个参数表示在同一条直线上进行碎线段连接的最大间隔值(gap), 即当同一条直线上的两条碎线段之间的间隔小于param2时，将其合二为一。
           */
          lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 80, 400, 10); // threshold, minLineLength, maxLineGap
          for (int i = 0; i < lines.total(); i++) {
              // Based on JavaCPP, the equivalent of the C code:
              // CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
              // CvPoint first=line[0], second=line[1]
              // is:
              Pointer line = cvGetSeqElem(lines, i);
              CvPoint pt1  = new CvPoint(line).position(0);
              CvPoint pt2  = new CvPoint(line).position(1);

              System.out.println("Line spotted: ");
              System.out.println("\t pt1: " + pt1);
              System.out.println("\t pt2: " + pt2);
              cvLine(colorDst, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0); // draw the segment on the image
          }
          cvSaveImage( "/Users/tangblack/Desktop/result_probabilistic.jpg" ,colorDst);  
      }
      /*
       * Apply the multiscale hough transform which returns for each line two float parameters (rho, theta)
       * rho: distance from the origin of the image to the line
       * theta: angle between the x-axis and the normal line of the detected line
       */
      else if(args.length==2 && args[1].contentEquals("multiscale")){
                      System.out.println("Using the multiscale Hough Transform"); //
          lines = cvHoughLines2(dst, storage, CV_HOUGH_MULTI_SCALE, 1, Math.PI / 180, 40, 1, 1);
          for (int i = 0; i < lines.total(); i++) {
              CvPoint2D32f point = new CvPoint2D32f(cvGetSeqElem(lines, i));

              float rho=point.x();
              float theta=point.y();

              double a = Math.cos((double) theta), b = Math.sin((double) theta);
              double x0 = a * rho, y0 = b * rho;
              CvPoint pt1 = cvPoint((int) Math.round(x0 + 1000 * (-b)), (int) Math.round(y0 + 1000 * (a))), pt2 = cvPoint((int) Math.round(x0 - 1000 * (-b)), (int) Math.round(y0 - 1000 * (a)));
              System.out.println("Line spoted: ");
              System.out.println("\t rho= " + rho);
              System.out.println("\t theta= " + theta);
              cvLine(colorDst, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0);
          }
          cvSaveImage( "/Users/tangblack/Desktop/result_multiscale.jpg" ,colorDst);  
      }
      /*
       * Default: apply the standard hough transform. Outputs: same as the multiscale output.
       */
      else {
          System.out.println("Using the Standard Hough Transform");
          lines = cvHoughLines2(dst, storage, CV_HOUGH_STANDARD, 1, Math.PI / 180, 90, 0, 0);
          for (int i = 0; i < lines.total(); i++) {
              CvPoint2D32f point = new CvPoint2D32f(cvGetSeqElem(lines, i));

              float rho=point.x();
              float theta=point.y();

              double a = Math.cos((double) theta), b = Math.sin((double) theta);
              double x0 = a * rho, y0 = b * rho;
              CvPoint pt1 = cvPoint((int) Math.round(x0 + 1000 * (-b)), (int) Math.round(y0 + 1000 * (a))), pt2 = cvPoint((int) Math.round(x0 - 1000 * (-b)), (int) Math.round(y0 - 1000 * (a)));
              System.out.println("Line spotted: ");
              System.out.println("\t rho= " + rho);
              System.out.println("\t theta= " + theta);
              cvLine(colorDst, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0);
          }
          cvSaveImage( "/Users/tangblack/Desktop/result.jpg" ,colorDst);  
      }
//      source.showImage(src);
//      hough.showImage(colorDst);

//      source.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//      hough.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	/**
	 * ronnywang/tw-campaign-finance
	 *
	 * @param fileName
	 * 
	 * @see <a href="https://github.com/ronnywang/tw-campaign-finance/blob/master/scripts/pic2linesjson.cpp">ronnywang/tw-campaign-finance</a>
	 * @see <a href="http://cg2010studio.wordpress.com/2013/03/22/opencv-iplimage-%E5%92%8C-mat-%E4%BA%92%E8%BD%89/">[OpenCV] IplImage 和 Mat 互轉</a>
	 */
	public static void execute2(String fileName,
			int threshold,
			int minLineLength,
			int maxLineGap)
	{
		/**
		 * #define CV_LOAD_IMAGE_UNCHANGED -1 原圖影像
		 * #define CV_LOAD_IMAGE_GRAYSCALE 0 灰階
		 * #define CV_LOAD_IMAGE_COLOR 1 彩色
		 * #define CV_LOAD_IMAGE_ANYDEPTH 2 任何彩度
		 * #define CV_LOAD_IMAGE_ANYCOLOR 4 任何彩色
		 */
		IplImage srcIplImage = cvLoadImage(fileName, CV_LOAD_IMAGE_COLOR); // filename, channel
		Mat src1 = new Mat(srcIplImage);
		Mat img = new Mat(srcIplImage);
		Mat output = src1;
		cvtColor(src1, img, CV_BGR2GRAY);
		Size size = new Size(3, 3);
		GaussianBlur(img, img, size, 0);
		adaptiveThreshold(img, img, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 75, 10);
		bitwise_not(img, img);
		
		IplImage imgIplImage = img.asIplImage();
		cvSaveImage("/Users/tangblack/Desktop/result_gray.jpg", imgIplImage);
		
		CvSeq lines = new CvSeq();
		CvMemStorage storage = cvCreateMemStorage(0);
		lines = cvHoughLines2(imgIplImage, 
				storage, 
				CV_HOUGH_PROBABILISTIC,
				1,
				Math.PI / 180, 
				threshold,
				minLineLength,
				maxLineGap);
		
		for (int i = 0; i < lines.total(); i++)
		{
			// Based on JavaCPP, the equivalent of the C code:
			// CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
			// CvPoint first=line[0], second=line[1]
			// is:
			Pointer line = cvGetSeqElem(lines, i);
			CvPoint pt1 = new CvPoint(line).position(0);
			CvPoint pt2 = new CvPoint(line).position(1);

			System.out.println("Line spotted: ");
			System.out.println("\t pt1: " + pt1);
			System.out.println("\t pt2: " + pt2);
			
			line(output, 
					new Point(pt1.x(), pt1.y()), 
					new Point(pt2.x(), pt2.y()), 
					new Scalar(CV_RGB(255, 0, 0)));
		}
		
		cvSaveImage("/Users/tangblack/Desktop/result_probabilistic.jpg", output.asIplImage());
	}
	
	/**
	 * My version.
	 * 
	 * @see <a href="http://www.360doc.com/content/13/1024/20/1771496_323843557.shtml">http://www.360doc.com/content/13/1024/20/1771496_323843557.shtml</a>
	 * 
	 */
	public static void execute3(String fileName,
			int threshold,
			int minLineLength,
			int maxLineGap)
	{
		/**
		 * #define CV_LOAD_IMAGE_UNCHANGED -1 原圖影像
		 * #define CV_LOAD_IMAGE_GRAYSCALE 0 灰階
		 * #define CV_LOAD_IMAGE_COLOR 1 彩色
		 * #define CV_LOAD_IMAGE_ANYDEPTH 2 任何彩度
		 * #define CV_LOAD_IMAGE_ANYCOLOR 4 任何彩色
		 */
		IplImage srcIplImage = cvLoadImage(fileName, CV_LOAD_IMAGE_COLOR); // filename, channel
//		IplImage srcGrayIplImage = cvLoadImage(fileName, CV_LOAD_IMAGE_GRAYSCALE);
		IplImage grayIplImage = cvCreateImage(cvGetSize(srcIplImage), IPL_DEPTH_8U, CV_LOAD_IMAGE_COLOR);
		IplImage cannyIplImage = cvCreateImage(cvGetSize(grayIplImage), grayIplImage.depth(), CV_LOAD_IMAGE_COLOR);
		
		cvCvtColor(srcIplImage, grayIplImage, CV_BGR2GRAY);
		cvCanny(grayIplImage, cannyIplImage, 20, 200, 3); // input, output, threshold1, threshold2, sobel
		
		CvSeq lines = new CvSeq();
		CvMemStorage storage = cvCreateMemStorage(0);
		lines = cvHoughLines2(cannyIplImage, 
				storage, 
				CV_HOUGH_PROBABILISTIC,
				1,
				Math.PI / 180, 
				threshold,
				minLineLength,
				maxLineGap);
		
		IplImage outputIplImage = cvCreateImage(cvGetSize(srcIplImage), IPL_DEPTH_8U, 3);
		cvCvtColor(grayIplImage, outputIplImage, CV_GRAY2BGR);
		Mat output = new Mat(outputIplImage);
		for (int i = 0; i < lines.total(); i++)
		{
			// Based on JavaCPP, the equivalent of the C code:
			// CvPoint* line = (CvPoint*)cvGetSeqElem(lines,i);
			// CvPoint first=line[0], second=line[1]
			// is:
			Pointer line = cvGetSeqElem(lines, i);
			CvPoint pt1 = new CvPoint(line).position(0);
			CvPoint pt2 = new CvPoint(line).position(1);

			System.out.println("Line spotted: ");
			System.out.println("\t pt1: " + pt1);
			System.out.println("\t pt2: " + pt2);
			
			
//			cvLine(outputIplImage, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0);
			line(output, 
					new Point(pt1.x(), pt1.y()), 
					new Point(pt2.x(), pt2.y()), 
					new Scalar(CV_RGB(255, 0, 0)));
		}
		
		cvSaveImage("/Users/tangblack/Desktop/result_gray.jpg", grayIplImage);
		cvSaveImage("/Users/tangblack/Desktop/result_canny.jpg", cannyIplImage);
//		cvSaveImage("/Users/tangblack/Desktop/result_probabilistic.jpg", outputIplImage);
		cvSaveImage("/Users/tangblack/Desktop/result_probabilistic.jpg", output.asIplImage());
	}
	
    /**
     * usage: java HoughLines imageDir\imageName TransformType
     */
    public static void main(String[] args) {
//    	execute(fileName, "probabilistic");
//    	execute(fileName, "multiscale");
//    	execute(fileName, "");
    	
//    	execute2("/Users/tangblack/Desktop/1393559100-1492277785.jpg", 80, 400, 10);
    	
    	execute3("/Users/tangblack/Desktop/144_02.jpg", 300, 50, 10);
    }
}
