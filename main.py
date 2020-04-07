''' import numpy as np
import cv2 as cv
img = cv.imread('2.jpg',0)
img = cv.medianBlur(img,5)
cimg = cv.cvtColor(img,cv.COLOR_GRAY2BGR)
circles = cv.HoughCircles(img,cv.HOUGH_GRADIENT,1.2,75)
                            #param1=0,param2=0,minRadius=0,maxRadius=0)
circles = np.uint16(np.around(circles[0, :]))
for (x, y, r) in circles:
    # draw the outer circle
    cv.circle(cimg,(x,y),r,(0,255,0),2)
    # draw the center of the circle
    cv.rectangle(cimg, (x - 5, y - 5), (x + 5, y + 5), (0, 128, 255), -1)
cv.imshow('detected circles',cimg)
cv.waitKey(0)
cv.destroyAllWindows()'''

import cv2
import numpy as np
import sys
import os
import imutils
import time

def resize_img(img, size=600):
    r = float(size) / img.shape[1]
    dim = (size, int(img.shape[0] * r))
    rimg = cv2.resize(img, dim, interpolation = cv2.INTER_AREA)

    return rimg

def get_circle(img):
    circles = cv2.HoughCircles(img, cv2.HOUGH_GRADIENT, 1, 50,
                                param1=50, param2=50,minRadius=10,maxRadius=200)

    if circles is None:
        return None

    circles = np.uint16(np.around(circles))
    largest = None
    num = 0
    rads = []
    for i in circles[0,:]:
        num += 1
        rads.append(i[2])

        if largest is None:
            largest = i
            continue
    
        if i[2] > largest[2] and i[2] < 100:
            largest = i

    # print('circles', num, largest, sorted(rads))
    return largest

def cropped_to_size(img, center, radius, height=500, width=500):
    buffer = 25
    if (radius > center[0]):
        x1 = 0
    else:
        x1 = max(0, center[0] - width//2)

    if (radius > center[1]):
        y1 = 0
    else:
        y1 = max(0, center[1] - height//2)

    x2 = min(center[0] + width//2, rimg.shape[1])
    y2 = min(center[1] + height//2, rimg.shape[1])

    if x1 == 0:
        x2 += (width - x2)
    if y1 == 0:
        y2 += (height - y2)
    
    crop_img = rimg[y1:y2, x1:x2]

    return crop_img

def cropped_to_img(img, center, radius, buffer=25):
    if (radius > center[0]):
        x1 = 0
    else:
        x1 = max(0, center[0] - radius - buffer)
    if (radius > center[1]):
        y1 = 0
    else:
        y1 = max(0, center[1] - radius - buffer)

    x2 = min(center[0] + radius + buffer, rimg.shape[1])
    y2 = min(center[1] + radius + buffer, rimg.shape[1])
    
    crop_img = rimg[y1:y2, x1:x2]

    return crop_img

for img_name in os.listdir(sys.argv[1]):

    print("hello", img_name)

    img = cv2.imread(sys.argv[1] + '/' + img_name, 0)

    rimg = resize_img(img, 650)
    circle = get_circle(rimg)
    print("circle", circle)

    if circle is None:
        print("No Circles:", img_name)
        continue
    
    radius = circle[2]
    center = (circle[0], circle[1])
  
    height = 250
    width = 250
    # rimg = cropped_to_size(rimg, center, radius, height, width)
    rimg = cropped_to_img(rimg, center, radius, buffer=50)
    # cv2.circle(rimg, center, radius, (0,255,0), 2)

    rimg = cv2.resize(rimg, (250, 250))
    # if crop_img.shape[0] != height and crop_img.shape[1] != width:
        # print(img_name, crop_img.shape)
        # continue

    # rotated_crop = imutils.rotate(crop_img, -90)
    
    cv2.startWindowThread()
    cv2.namedWindow("preview")
    cv2.imwrite('resized_images/0_' + img_name, rimg)
    cv2.imshow('detected circles', rimg)
    print("imshow", circle)
    cv2.waitKey(0)
    # if cv2.waitKey(1) & 0xFF == ord('q'):
    #     break

    # time.sleep(1)

# cv2.destroyAllWindows()