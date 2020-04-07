#!/usr/bin/env python
# -*- coding: utf-8 -*-
# vim:fileencoding=utf-8

import sys
import numpy as np
import cv2 as cv
import os

import easygui
import unicodedata

from os import listdir
from os.path import isfile, join

cv.namedWindow("settings")  # создаем окно настроек

# прибавка к минимальной яркости - вводится с помощью клавиш 3,4
inc = 0

# ползунки минимальной и максимальной яркости
minV = 55
maxV = 255

# размер ядра
ksize = 3


def onChangeMinV(*arg):
    minV = arg
    pass


def onChangeMaxV(*arg):
    maxV = arg
    pass


def onChangeKsize(*arg):
    if arg % 2 == 0:
        pass
    ksize = arg


cv.createTrackbar("minimal Brightness", "settings", 55, 255, onChangeMinV)
cv.createTrackbar("maximum Brightness", "settings", 255, 255, onChangeMaxV)
cv.createTrackbar("kernel size", "settings", 3, 21, onChangeKsize)
# create switch for ON/OFF functionality

# Показывать все прямоугольники(красный цвет)
showR = 1
# Показывать прямоугольники с наибольшим количеством фильтров(зеленый цвет)
showG = 1
# Показывать основные прямоугольники(синий цвет)
showB = 1

# Выбрать какие прямоугольники будут сохранены
saveT = 1


def onChangeShowR(*arg):
    showR = arg


def onChangeShowG(*arg):
    showG = arg


def onChangeShowB(*arg):
    showB = arg


cv.createTrackbar("Show all(red)", "settings", 0, 1, onChangeShowR)
cv.createTrackbar("Show filtered(blue)", "settings", 0, 1, onChangeShowG)
cv.createTrackbar("Show hard filtered(green)", "settings", 0, 1, onChangeShowB)

showOrigin = 1
showThresh = 0
showContours = 1


def onChangeShowOrigin(*arg):
    showOrigin = arg


def onChangeShowThresh(*arg):
    showThresh = arg


def onChangeShowContours(*arg):
    showContours = arg


# Цвета
RED = (0, 0, 255)
BLUE = (255, 0, 0)
GREEN = (0, 255, 0)

# Создаем из контуров прямоугольники, чтобы в последствии сделать фотки
def makeRects(rimg, contours):
    rects = []
    for cnt in contours:
        rect = cv.minAreaRect(cnt)  # пытаемся вписать прямоугольник
        rects.append(rect)
    return rects


def makeBoxes(rimg, rects):
    boxes = []
    for rect in rects:
        box = cv.boxPoints(rect)  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат
        boxes.append(box)
    return boxes


# инициализируем цвет прямоугольников красным
def markAll(rects):
    return list(map(lambda x: [x, RED], rects))

def filterLight(сrect):
    rect = сrect[0]
    if rect[1][0] < 100 and rect[2] % 10 == 0:
        return [rect, BLUE]
    return сrect


# инициализируем цвет прямоугольника сними
def markBlue(crects):
    return list(map(filterLight, crects))


def markGreen(crects):
    W, H = 0, 0
    rects = []
    for crect in crects:
        rect = crect[0]
        rects.append(rect)
        W += rect[1][0]
        H += rect[1][1]

    if len(rects) != 0:
        W /= len(rects)
        H /= len(rects)

    s_min = W * H / 2
    s_max = W * H * 4
    print("GREEEEEN", s_min, s_max)

    for crect in crects:
      
        box = cv.boxPoints(crect[0])  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат

        s = crect[0][1][0] * crect[0][1][1]
        print("GREEEEEN", s_min, s_max)
        if s > s_min and s < s_max and rect[2] % 10 == 0:
            print("GREEEEEN", "GREEEEEN")
            crect[1] = GREEN
           
   
    return crects

    # W, H = 0, 0
    # for rect in crects:
    #     print("rect", rect)
    #     W += rect[0][1][0]
    #     H += rect[0][1][1]

    # if len(crects) != 0:
    #     W /= len(crects)
    #     H /= len(crects)

    # s_min = W * H / 2
    # s_max = W * H * 4

    # def f(сrect):
    #     rect = сrect[0]
    #     color = сrect[1]
    #     s = rect[1][0] * rect[1][1]
    #     if s > s_min and s < s_max and rect[2] % 10 == 0:
    #         return [rect, GREEN]
    #     return [rect, color]

    # return list(filter(f, crects))


# def drawAll(rimg, contours):
#     for cnt in contours0:
#         rect = cv.minAreaRect(cnt)  # пытаемся вписать прямоугольник
#         box = cv.boxPoints(rect)  # поиск четырех вершин прямоугольника
#         box = np.int0(box)  # округление координат
#         cv.drawContours(rimg, [box], 0, (0, 0, 255), 2)  # рисуем прямоугольник
#     cv.imshow("rimg", rimg)  # вывод обработанного кадра в окно

# def drawAll(rimg, contours):
#     for cnt in contours0:
#         rect = cv.minAreaRect(cnt)  # пытаемся вписать прямоугольник
#         box = cv.boxPoints(rect)  # поиск четырех вершин прямоугольника
#         box = np.int0(box)  # округление координат
#         cv.drawContours(rimg, [box], 0, (0, 0, 255), 2)  # рисуем прямоугольник
#     cv.imshow("rimg", rimg)  # вывод обработанного кадра в окно


def draw(rimg, boxes, crects):
    for crect in crects:
        box = cv.boxPoints(crect[0])  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат
        cv.drawContours(rimg, [box], 0, crect[1], 2)  # рисуем прямоугольник
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

    while True:

        frame = cv.imread(fn, 0)

        cv.imshow("origin", frame)

        edges = cv.Canny(frame, 120, 110)
        # kernel is used to control the amount of eroding and dilating
        kernel2 = np.ones((5, 5), np.uint8)  # np.uint8
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
        kernel = np.array([[0, 1, 0], [1, 1, 1], [0, 1, 0],], dtype=np.uint8,)
        # blur = cv.blur(frame,(5,5))
        img_erode = cv.erode(frame, kernel, iterations=2)
        img_erode2 = cv.erode(frame, kernel1, iterations=2)
        img_erode3 = cv.erode(frame, kernel2, iterations=2)
        # img_dilate = cv.dilate(frame, kernel, iterations=2)
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
            (255, 255, inc + minV), np.uint8
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
            if inc + cv.getTrackbarPos("minimal Brightness", "settings") - 1 > 0:
                inc -= 1

        if ch == 52:
            if inc + cv.getTrackbarPos("minimal Brightness", "settings") + 1 < 255:
                inc += 1

        if ch == 82:
            fn = easygui.fileopenbox()

        rects = makeRects(rimg, contours0)
        boxes = makeBoxes(rimg, rects)
        crects = markAll(rects)
        crects = markBlue(crects)
        crects = markGreen(crects)
        draw(rimg, boxes, crects)

        # drawAll(rimg, contours0)
        # drawAllContours(rimg, contours0)
        # drawMiddleContours(rimg, contours0, ch == 13)

        if ch == 27:
            break

    cv.destroyAllWindows()
