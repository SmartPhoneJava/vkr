import sys
import numpy as np
import cv2 as cv

# параметры цветового фильтра
hsv_min = np.array((0, 0, 0), np.uint8)
hsv_max = np.array((255, 255, 59), np.uint8)

if __name__ == '__main__':
    print(__doc__)

    fn = 'Best/2.png' # путь к файлу с картинкой
    img = cv.imread(fn)
    cv.imshow('origin',img)

    # hsv = cv.cvtColor( img, cv.COLOR_BGR2HSV ) # меняем цветовую модель с BGR на HSV 
    # thresh = cv.inRange( hsv, hsv_min, hsv_max ) # применяем цветовой фильтр
    # # ищем контуры и складируем их в переменную contours
    # contours0, hierarchy = cv.findContours( thresh.copy(), cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE)

    # for cnt in contours0:
    #         rect = cv.minAreaRect(cnt) # пытаемся вписать прямоугольник
    #         box = cv.boxPoints(rect) # поиск четырех вершин прямоугольника
    #         box = np.int0(box) # округление координат
    #         cv.drawContours(img,[box],0,(255,0,0),2) # рисуем прямоугольник
    # # отображаем контуры поверх изображения
    # #cv.drawContours( img, contours, -1, (255,0,0), 2, cv.LINE_AA, hierarchy, 1 )
    # cv.imshow('contours', img) # выводим итоговое изображение в окно

    # # detect circles

    # src = cv.imread(fn)
    # gray = cv.cvtColor(src, cv.COLOR_BGR2GRAY)
    
    
    # gray = cv.medianBlur(gray, 5)
    
    
    # rows = gray.shape[0]
    # circles = cv.HoughCircles(gray, cv.HOUGH_GRADIENT, 1, rows / 8,
    #                            param1=100, param2=30,
    #                            minRadius=1, maxRadius=50)
    
    
    # print("circles", circles)
    # if circles is not None:
    #     circles = np.uint16(np.around(circles))
    #     for i in circles[0, :]:
    #         center = (i[0], i[1])
    #         # circle center
    #         cv.circle(src, center, 1, (0, 100, 100), 3)
    #         # circle outline
    #         radius = i[2]
    #         cv.circle(src, center, radius, (255, 0, 255), 3)
    
    
    # cv.imshow("detected circles", src)

    hsv = cv.cvtColor( img, cv.COLOR_BGR2HSV ) # меняем цветовую модель с BGR на HSV 
    thresh = cv.inRange( hsv, hsv_min, hsv_max ) # применяем цветовой фильтр

    cv.imshow('thresh', thresh) # вывод обработанного кадра в окно
    
    edges = cv.Canny(thresh,120,110)
    cv.imshow('edges', edges)

    contours0, hierarchy = cv.findContours(edges, cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE)
    cv.drawContours( img, contours0, -1, (255,0,0), 3, cv.LINE_AA, hierarchy, 1 )
    cv.imshow('contours', img) # вывод обработанного кадра в окно

    #

    cv.waitKey()
    cv.destroyAllWindows()