#!/usr/bin/env python
# -*- coding: utf-8 -*-
# vim:fileencoding=utf-8

import sys
import numpy as np
import cv2 as cv
import os
import random
from datetime import datetime

import easygui
import unicodedata

import json

from os import listdir
from os.path import isfile, join

v2 = cv.CascadeClassifier('./../../cascade2.xml')
v4 = cv.CascadeClassifier('./../../cascade4.xml')
v5 = cv.CascadeClassifier('./../../cascade5.xml')
# eye_cascade = cv.CascadeClassifier('haarcascade_eye.xml')


settings_window = "findContours settings"
cv.namedWindow(settings_window, cv.WINDOW_AUTOSIZE)  # создаем окно настроек

cascade_settings_window = "haar cascade settings"
# создаем окно настроек
cv.namedWindow(cascade_settings_window, cv.WINDOW_AUTOSIZE)

# прибавка к минимальной яркости - вводится с помощью клавиш 3,4
inc = 0

settings_file = './../../settings.json'
with open(settings_file) as f:
    loaded = json.load(f)


def nothing(*arg):
    pass


# ползунки минимальной и максимальной яркости
json_minV = 'minV'
json_maxV = 'maxV'
# размер ядра
json_ksize = 'ksize'

v_min_text = "minimal Brightness"
v_max_text = "maximum Brightness"
k_size_text = "kernel size"

cv.createTrackbar(v_min_text, settings_window, loaded[json_minV], 255, nothing)
cv.createTrackbar(v_max_text, settings_window, loaded[json_maxV], 255, nothing)
cv.createTrackbar(k_size_text, settings_window,
                  loaded[json_ksize], 21, nothing)


def getMinBrightness():
    return cv.getTrackbarPos(v_min_text, settings_window)


def getMaxBrightness():
    return cv.getTrackbarPos(v_max_text, settings_window)


def getKernelSize():
    return cv.getTrackbarPos(k_size_text, settings_window)


json_ktype = 'ktype'

ktype_text = "kernel type(0-rhombus, 1-square, 2-plus)"

# типы ядра
ROMB = 0
SQUARE = 1
PLUS = 22
RANDOM = 3

cv.createTrackbar(ktype_text, settings_window, loaded[json_ktype], 3, nothing)


def getKtype():
    return cv.getTrackbarPos(ktype_text, settings_window)


randomApplied = 0

# Показывать все прямоугольники(красный цвет)
json_showR = 'red_visible'
# Показывать прямоугольники с наибольшим количеством фильтров(зеленый цвет)
json_showG = 'green_visible'
# Показывать основные прямоугольники(синий цвет)
json_showB = 'blue_visible'

red_text = "Show all(red)"
blue_text = "Show filtered(blue)"
green_text = "Show hard filtered(green)"

cv.createTrackbar(red_text, settings_window, loaded[json_showR], 1, nothing)
cv.createTrackbar(blue_text, settings_window, loaded[json_showB], 1, nothing)
cv.createTrackbar(green_text, settings_window, loaded[json_showG], 1, nothing)


def getShowRed():
    return cv.getTrackbarPos(red_text, settings_window)


def getShowBlue():
    return cv.getTrackbarPos(blue_text, settings_window)


def getShowGreen():
    return cv.getTrackbarPos(green_text, settings_window)


json_min_s = 'minS'
json_max_s = 'maxS'

min_s_text = "minimum square(* x0.01)"
max_s_text = "maximum square(* x0.1)"

cv.createTrackbar(min_s_text,
                  settings_window, loaded[json_min_s], 2000, nothing)
cv.createTrackbar(max_s_text,
                  settings_window, loaded[json_max_s], 300, nothing)


def getMinS():
    return 0.01*cv.getTrackbarPos(min_s_text, settings_window)


def getMaxS():
    return 0.1*cv.getTrackbarPos(max_s_text, settings_window)


json_deviation = 'deviation'
deviation_text = "max deviation from square (*0.1)"

cv.createTrackbar(deviation_text,
                  settings_window, loaded[json_deviation], 50, nothing)


def getDeviation():
    return 0.1*cv.getTrackbarPos(deviation_text, settings_window)


origin_text = "Show Origin"
thresh_text = "Show White-Black"
contours_text = "Show Contours"

json_origin = 'origin_show'
json_thresh = 'thresh_show'
json_contours = 'contours_show'

cv.createTrackbar(origin_text, settings_window,
                  loaded[json_origin], 1, nothing)
cv.createTrackbar(thresh_text, settings_window,
                  loaded[json_thresh], 1, nothing)
cv.createTrackbar(contours_text, settings_window,
                  loaded[json_contours], 1, nothing)


def getOrigin():
    return cv.getTrackbarPos(origin_text, settings_window)


def getThresh():
    return cv.getTrackbarPos(thresh_text, settings_window)


def getCountours():
    return cv.getTrackbarPos(contours_text, settings_window)


scale_factor_text = "Scale factor"
min_neighbors_text = "Minimum neighbors"
min_size = "Minimum size "
max_size = "Maximum size "

cv.createTrackbar(scale_factor_text, cascade_settings_window, 100, 100, nothing)
cv.createTrackbar(min_neighbors_text, cascade_settings_window, 20, 30, nothing)
cv.createTrackbar(min_size, cascade_settings_window, 5, 50, nothing)
cv.createTrackbar(max_size, cascade_settings_window, 500, 500, nothing)

def getScaleFactor():
    v = 0.1*cv.getTrackbarPos(scale_factor_text, cascade_settings_window)
    if v < 1.1:
        v = 1.05
    # print("v", v)
    return v


def getMinNeighbors():
    return cv.getTrackbarPos(min_neighbors_text, cascade_settings_window)


def getMinSize():
    s = cv.getTrackbarPos(min_size, cascade_settings_window)
    return (s, s)

def getMaxSize():
    s = cv.getTrackbarPos(max_size, cascade_settings_window)
    return (s, s)


v1_text = "Show cascade, using origin photo"
v2_text = "Show cascade, using eroding photo"
v3_text = "Show cascade, using hsv photo"

cv.createTrackbar(v1_text, cascade_settings_window, 1, 1, nothing)
cv.createTrackbar(v2_text, cascade_settings_window, 1, 1, nothing)
cv.createTrackbar(v3_text, cascade_settings_window, 1, 1, nothing)


def getShowV1():
    return cv.getTrackbarPos(v1_text, cascade_settings_window)


def getShowV2():
    return cv.getTrackbarPos(v2_text, cascade_settings_window)


def getShowV3():
    return cv.getTrackbarPos(v3_text, cascade_settings_window)


'''
# Показывать все прямоугольники(рыжий цвет)
json_showO = 'orange_visible'
# Показывать прямоугольники с наибольшим количеством фильтров(зеленый цвет)
json_showW = 'white_visible'
# Показывать основные прямоугольники(синий цвет)
json_showB = 'yellow_visible'

red_text = "Show all(red)"
blue_text = "Show filtered(blue)"
green_text = "Show hard filtered(green)"

cv.createTrackbar(red_text, settings_window, loaded[json_showR], 1, nothing)
cv.createTrackbar(blue_text, settings_window, loaded[json_showB], 1, nothing)
cv.createTrackbar(green_text, settings_window, loaded[json_showG], 1, nothing)


def getShowRed():
    return cv.getTrackbarPos(red_text, settings_window)


def getShowBlue():
    return cv.getTrackbarPos(blue_text, settings_window)


def getShowGreen():
    return cv.getTrackbarPos(green_text, settings_window)
'''

# json_min_s = 'minS'
# json_max_s = 'maxS'

# min_s_text = "minimum square(* x0.01)"
# max_s_text = "maximum square(* x0.1)"

# cv.createTrackbar(min_s_text,
#                   settings_window, loaded[json_min_s], 2000, nothing)
# cv.createTrackbar(max_s_text,
#                   settings_window, loaded[json_max_s], 300, nothing)


# Цвета
RED = (0, 0, 255)
BLUE = (255, 0, 0)
GREEN = (0, 255, 0)

COLOR_V1 = (79, 39, 171)  # Амарантово-пурпурный
COLOR_V2 = (187, 156, 241)# Амарантовый
COLOR_V3 = (212, 255, 127)# Аквамариновый
COLOR_V4 = (177, 206, 251)# Абрикосовый

def filterLight(сrect):
    rect = сrect[0]
    if rect[1][0] < 100 and (rect[2] % 10 == 0 or getKtype() != SQUARE):
        return [rect, BLUE]
    return сrect


def canShow(color):
    return (color == RED and getShowRed()) or (color == GREEN and getShowGreen()) or (color == BLUE and getShowBlue())


def draw(name, rimg, boxes, crects):
    for crect in crects:
        box = cv.boxPoints(crect[0])  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат

        color = crect[1]
        if canShow(color):
            # print("box", crect[0])
            cv.drawContours(rimg, [box], 0, color,  2)  # рисуем прямоугольник
    cv.imshow(name, rimg)  # вывод обработанного кадра в окно

def usualKernel(ksize):
    return np.ones((ksize, ksize), np.uint8)


def rombKernel(ksize):
    e = ksize*2-1
    arr = []
    if ksize % 2 != 1:
        ksize = ksize + 1

    for i in range(1, ksize, 2):
        i = '0 ' * ((ksize-i)//2) + '1 ' * i + '0 ' * (ksize-i)
        i = i[:e]
        arr.append(list(map(int, i.split(' '))))
    for i in range(ksize, 0, -2):
        i = '0 ' * ((ksize-i)//2) + '1 ' * i + '0 ' * (ksize-i)
        i = i[:e]
        arr.append(list(map(int, i.split(' '))))
    return np.array(arr, np.uint8)


def krestKernel(ksize):
    kernel = []
    m = int(ksize/2)
    for i in range(ksize):
        arr = []
        for j in range(ksize):
            if i == m or j == m:
                arr.append(np.uint8(1))
            else:
                arr.append(np.uint8(0))
        kernel.append(arr)
    return np.array(kernel, np.uint8)


def drawOrigin(frame):
    if getOrigin():
        cv.imshow("origin", frame)
    else:
        cv.destroyWindow("origin")


def drawThresh(frame):
    if getThresh():
        cv.imshow("black and white", frame)
    else:
        cv.destroyWindow("black and white")


def drawCountours(frame, contours0):
    if getCountours():
        cv.drawContours(
            frame, contours0, -1, (255, 255, 0), 3, cv.LINE_AA, hierarchy, 1
        )
        cv.imshow("countours", frame)
    else:
        cv.destroyWindow("countours")


def drawCascade(img, boxes, color):
    for (x, y, w, h) in boxes:
        cv.rectangle(img, (x, y), (x+w, y+h), color, 2)

    if len(img) > 0:
        cv.imshow('img', img)
    else:
        print("no image")


def loadDefault():
    cv.setTrackbarPos(v_min_text, settings_window, 0)
    cv.setTrackbarPos(v_max_text, settings_window, 255)
    cv.setTrackbarPos(k_size_text, settings_window, 5)
    cv.setTrackbarPos(red_text, settings_window, 1)
    cv.setTrackbarPos(blue_text, settings_window, 1)
    cv.setTrackbarPos(green_text, settings_window, 1)
    cv.setTrackbarPos(min_s_text, settings_window, 1)
    cv.setTrackbarPos(max_s_text, settings_window, 100)
    cv.setTrackbarPos(deviation_text, settings_window,
                      100)
    cv.setTrackbarPos(origin_text, settings_window, 0)
    cv.setTrackbarPos(thresh_text, settings_window, 0)
    cv.setTrackbarPos(contours_text, settings_window, 0)
    cv.setTrackbarPos(ktype_text, settings_window, SQUARE)


def getConfigFilename(configsPath, onlyfiles, index):
    return configsPath + "/"+onlyfiles[index].split(".")[0]+".json"


pathImage = ""
newPathConfig = ""

def saveAndLoad(needSave, configsPath, onlyfiles, index):
    global pathImage
    global newPathConfig

    print("path", imagesPath + "/" + onlyfiles[index])
    pathImage = imagesPath + "/" + onlyfiles[index]
    newPathConfig = getConfigFilename(configsPath, onlyfiles, index)

    return pathImage


def loadMeta(settings_file):
    with open(settings_file) as f:
        loaded = json.load(f)
        cv.setTrackbarPos(v_min_text, settings_window, loaded[json_minV])
        cv.setTrackbarPos(v_max_text, settings_window, loaded[json_maxV])
        cv.setTrackbarPos(k_size_text, settings_window, loaded[json_ksize])
        cv.setTrackbarPos(red_text, settings_window, loaded[json_showR])
        cv.setTrackbarPos(blue_text, settings_window, loaded[json_showB])
        cv.setTrackbarPos(green_text, settings_window, loaded[json_showG])
        cv.setTrackbarPos(min_s_text, settings_window, loaded[json_min_s])
        cv.setTrackbarPos(max_s_text, settings_window, loaded[json_max_s])
        cv.setTrackbarPos(deviation_text, settings_window,
                          loaded[json_deviation])
        cv.setTrackbarPos(origin_text, settings_window, loaded[json_origin])
        cv.setTrackbarPos(thresh_text, settings_window, loaded[json_thresh])
        cv.setTrackbarPos(contours_text, settings_window,
                          loaded[json_contours])
        cv.setTrackbarPos(ktype_text, settings_window, loaded[json_ktype])


def round(configsPath, frame, file_list, index):
    pathImage = saveAndLoad(False, configsPath, file_list, index)
    if len(frame) == 0:
        frame = cv.imread(pathImage, 0)
    boxes = v2.detectMultiScale(frame, scaleFactor=getScaleFactor(),
                                minNeighbors=getMinNeighbors(),
                                minSize=getMinSize(),
                                maxSize=getMaxSize())
    return pathImage, frame, boxes



imagesPath = "/home/artyom/labs/bauman/1/vkr/Best"
savingPath = "/home/artyom/labs/bauman/1/vkr/Good"
configsPath = "/home/artyom/labs/bauman/1/vkr/output/configuration"

if __name__ == "__main__":
    print(__doc__)

    onlyfiles = [f for f in listdir(imagesPath) if isfile(join(imagesPath, f))]
    index = 0

    pathImage, frame, boxes = round(configsPath, [], onlyfiles, index)

    while True:
        pathImage, frame, boxes = round(configsPath, [], onlyfiles, index)

        drawOrigin(frame)
        ksize = getKernelSize()
        kernel = usualKernel(ksize)
        if getKtype() == PLUS:
            kernel = krestKernel(ksize)
        elif getKtype() == ROMB:
            kernel = rombKernel(ksize)

        img_erode = cv.erode(frame, kernel, iterations=2)

        # параметры цветового фильтра
        hsv_min = np.array((0, 0, inc + getMinBrightness()), np.uint8)
        # экспериментальным путем получено 71
        hsv_max = np.array((255, 255, getMaxBrightness()), np.uint8)

        hsvPath = os.path.join(
            "/home/artyom/labs/bauman/1/vkr/output/1", "1.jpg")
        cv.imwrite(hsvPath, img_erode)

        new_img = cv.imread(hsvPath)
        # меняем цветовую модель с BGR на HSV
        hsv = cv.cvtColor(new_img, cv.COLOR_BGR2HSV)

        _, _, boxes1 = round(configsPath, img_erode, onlyfiles, index)

        _, _, boxes2 = round(configsPath, hsv, onlyfiles, index)

        frame = cv.imread(pathImage)
        if getShowV1():
            drawCascade(frame, boxes, COLOR_V1)
        if getShowV2():
            drawCascade(frame, boxes1, COLOR_V2)
        if getShowV3():
            drawCascade(frame, boxes2, COLOR_V3)

        # path = os.path.join("/home/artyom/labs/bauman/vkr/output/2", "1.jpg")
        # cv.imwrite(path, thresh)

        ch = cv.waitKey(5)
        print("ch", ch)  # 13 81 83

        if ch == 49:
            if index > 0:
                print("index", index, len(onlyfiles))
                index -= 1
                cv.destroyWindow(pathImage)
                pathImage, frame, boxes = round(configsPath, [], onlyfiles, index)
        elif ch == 50:
            if index < len(onlyfiles) - 1:
                print("index", index, len(onlyfiles))
                index += 1
                cv.destroyWindow(pathImage)
                pathImage, frame, boxes = round(configsPath, [], onlyfiles, index)

        if ch == 27:
            break

    cv.destroyAllWindows()
