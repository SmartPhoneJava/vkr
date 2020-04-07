import cv2
import numpy as np

cap = cv2.VideoCapture(0)

hsv_min = np.array((0, 77, 17), np.uint8)
hsv_max = np.array((208, 255, 255), np.uint8)

while(1):
   #_, frame = cap.read()  
    
   frame = cv2.imread("Best/Picture 165.jpg", 0)
  
   cv2.imshow('Original',frame)
   edges = cv2.Canny(frame,120,110)
   cv2.imshow('Canny',edges)
   k = cv2.waitKey(5) & 0xFF
   if k == 27:
      break
   # contours0, hierarchy = cv2.findContours(edges, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
   # for cnt in contours0:
   #     if len(cnt)>4:
   #         ellipse = cv.fitEllipse(cnt)
   #         cv.ellipse(img,ellipse,(0,0,255),2)

   # cv.imshow('contours', img)
   # cv2.drawContours( edges, contours0, -1, (255,255,0), 1, cv2.LINE_AA, hierarchy, 1 )
   # cv2.imshow('contours', edges) # выводим итоговое изображение в окно

   #    img = cv2.imread('1.jpg', 1)
   # path = 'D:/OpenCV/Scripts/Images'
   # cv2.imwrite(os.path.join(path , 'waka.jpg'), img)

cv2.destroyAllWindows()
cap.release()