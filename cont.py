# -*- coding: cp1251 -*-
import sys
import numpy as np
import cv2 as cv
import os

import easygui
import unicodedata

from os import listdir
from os.path import isfile, join


def nothing(*arg):
    pass


print("привет мир")
cv.namedWindow("settings")  # создаем окно настроек
cv.createTrackbar("вввввв", "settings", 55, 255, nothing)
# create switch for ON/OFF functionality
switch = 'Отобразить'
cv.createTrackbar('Show ', 'settings',0,1,nothing)


def drawAll(rimg, contours):
    for cnt in contours0:
        rect = cv.minAreaRect(cnt)  # пытаемся вписать прямоугольник
        box = cv.boxPoints(rect)  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат
        cv.drawContours(rimg, [box], 0, (0, 0, 255), 2)  # рисуем прямоугольник
    cv.imshow("rimg", rimg)  # вывод обработанного кадра в окно


def drawAllContours(rimg, contours):
    for cnt in contours0:
        rect = cv.minAreaRect(cnt)  # пытаемся вписать прямоугольник
        box = cv.boxPoints(rect)  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат
        if rect[1][0] < 100 and rect[2] % 10 == 0:
            cv.drawContours(rimg, [box], 0, (255, 0, 0), 2)  # рисуем прямоугольник
    cv.imshow("rimg", rimg)  # вывод обработанного кадра в окно


def drawMiddleContours(rimg, contours, r):
    W, H = 0, 0
    rects = []
    for cnt in contours0:
        rect = cv.minAreaRect(cnt)  # пытаемся вписать прямоугольник
        rects.append(rect)
        W += rect[1][0]
        H += rect[1][1]

    if len(rects) != 0:
        W /= len(rects)
        H /= len(rects)

    s_min = W * H / 2
    s_max = W * H * 4

    for rect in rects:
        box = cv.boxPoints(rect)  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат

        s = rect[1][0] * rect[1][1]
        if s > s_min and s < s_max and rect[2] % 10 == 0:
            cv.drawContours(rimg, [box], 0, (0, 255, 0), 2)  # рисуем прямоугольник
    cv.imshow("rimg", rimg)  # вывод обработанного кадра в окно


if __name__ == "__main__":
    print(__doc__)

    fn = "Best/Picture 228.jpg"  # путь к файлу с картинкой
    img = cv.imread(fn)

    # uni_code = easygui.fileopenbox()
    # img_path = uni_code
    # fn = img_path
    # print("img_path", img_path, uni_code)

    mypath = "/home/artyom/labs/bauman/vkr/Best"
    onlyfiles = [f for f in listdir(mypath) if isfile(join(mypath, f))]
    print("onlyfiles", onlyfiles)
    index = 0
    fn = mypath + "/" + onlyfiles[index]

    inc = 0

    while True:

        frame = cv.imread(fn, 0)

        cv.imshow("origin", frame)

        edges = cv.Canny(frame, 120, 110)
        # kernel is used to control the amount of eroding and dilating
        kernel2 = np.ones((5, 5), np.uint8)#np.uint8
        kernel1 = np.array(
            [
                [0, 0, 1, 0, 0],
                [0, 1, 1, 1, 0],
                [1, 1, 1, 1, 1],
                [0, 1, 1, 1, 0],
                [0, 0, 1, 0, 0],
            ],
            dtype=np.uint8,
        )
        kernel = np.array(
            [
                [0, 1, 0],
                [1, 1, 1],
                [0, 1, 0],
            ],
            dtype=np.uint8,
        )
        #blur = cv.blur(frame,(5,5))
        img_erode =  cv.erode(frame, kernel, iterations=2)
        img_erode2 =  cv.erode(frame, kernel1, iterations=2)
        img_erode3 = cv.erode(frame, kernel2, iterations=2)
        #img_dilate = cv.dilate(frame, kernel, iterations=2)
        # cv.imshow("img_dilate1", img_erode)
        # cv.imshow("img_dilate2", img_erode2)
        # cv.imshow("img_erode", img_erode)
        img_erode3 = img_erode

        # # Repeat the erosion and dilation by changing iterations.
        # img_erode = cv.erode(frame, kernel, iterations=2)
        # cv.imshow("img_erode", img_erode)

        # параметры цветового фильтра
        hsv_min = np.array((0, 0, 0), np.uint8)
        # экспериментальным путем получено 71
        hsv_max = np.array(
            (255, 255, inc + cv.getTrackbarPos("v1", "settings")), np.uint8
        )

        path = os.path.join("/home/artyom/labs/bauman/vkr/output/1", "1.jpg")
        cv.imwrite(path, img_erode)

        new_img = cv.imread(path)
        hsv = cv.cvtColor(
            new_img, cv.COLOR_BGR2HSV
        )  # меняем цветовую модель с BGR на HSV
        thresh = cv.inRange(hsv, hsv_min, hsv_max)  # применяем цветовой фильтр
        # cv.imshow('thresh', thresh) # вывод обработанного кадра в окно

        # cv.imshow("thresh", thresh)
        img_dilate = cv.dilate(img_erode, kernel, iterations=2)
        # cv.imshow('img_erode',img_erode)
        # cv.imshow('img_dilate',img_dilate)

        # canny = cv.Canny(img_erode,120,110)
        # cv.imshow('canny',canny)

        contours0, hierarchy = cv.findContours(
            thresh, cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE
        )

        new_img2 = cv.imread(fn)
        cv.drawContours(
            new_img2, contours0, -1, (255, 255, 0), 3, cv.LINE_AA, hierarchy, 1
        )
        # cv.imshow("contours", new_img2)  # вывод обработанного кадра в окно

        path = os.path.join("/home/artyom/labs/bauman/vkr/output/2", "1.jpg")
        cv.imwrite(path, thresh)

        rimg = cv.imread(fn)

        #

        ch = cv.waitKey(5)
        print("ch", ch)  # 13 81 83

        if ch == 49:
            if index > 0:
                index -= 1
                fn = mypath + "/" + onlyfiles[index]

        if ch == 50:
            if index < len(onlyfiles) - 1:
                index += 1
                fn = mypath + "/" + onlyfiles[index]

        if ch == 51:
            if inc + cv.getTrackbarPos("v1", "settings") - 1 > 0:
                inc -= 1

        if ch == 52:
            if inc + cv.getTrackbarPos("v1", "settings") + 1 < 255:
                inc += 1

        if ch == 82:
            fn = easygui.fileopenbox()

        drawAll(rimg, contours0)
        drawAllContours(rimg, contours0)
        drawMiddleContours(rimg, contours0, ch == 13)

        if ch == 27:
            break

    cv.destroyAllWindows()
