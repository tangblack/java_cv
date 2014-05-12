package com.tangblack.javacv.example;
import  static  org.bytedeco.javacpp.opencv_core.*;  
import  static  org.bytedeco.javacpp.opencv_imgproc.*;  
import  static  org.bytedeco.javacpp.opencv_highgui.*;  

  
public  class  Smoother {  
	
	public  static  void  main(String args[]){  
        Smoother.smooth( "/Users/tangblack/Desktop/1_victory06057231.jpg" );  
    }  
      
    public  static  void  smooth(String filename){  
        IplImage image=cvLoadImage(filename);  
        if (image!= null ){  
            cvSmooth(image,image);  
//            cvSmooth(image,image,CV_GAUSSIAN, 3 );  
            cvSaveImage( "/Users/tangblack/Desktop/1_victory06057231_new.jpg" ,image);  
            cvReleaseImage(image);  
        }  
    }  
  
}