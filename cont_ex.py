#!/usr/bin/env python
# -*- coding: utf-8 -*-
# vim:fileencoding=utf-8

import sys
import numpy as np
import cv2 as cv
import os

import easygui
import unicodedata

import json

from os import listdir
from os.path import isfile, join

settings_window = "settings"
cv.namedWindow(settings_window, cv.WINDOW_AUTOSIZE)  # создаем окно настроек

# прибавка к минимальной яркости - вводится с помощью клавиш 3,4
inc = 0

settings_file = './settings.json'
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

cv.createTrackbar(ktype_text, settings_window, loaded[json_ktype], 2, nothing)


def getKtype():
    return cv.getTrackbarPos(ktype_text, settings_window)


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

min_s_text = "minimum square(/ x0.1)"
max_s_text = "maximum square(* x0.1)"

cv.createTrackbar(min_s_text,
                  settings_window, loaded[json_min_s], 100, nothing)
cv.createTrackbar(max_s_text,
                  settings_window, loaded[json_max_s], 300, nothing)


def getMinS():
    return 0.1*cv.getTrackbarPos(min_s_text, settings_window)


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


# Цвета
RED = (0, 0, 255)
BLUE = (255, 0, 0)
GREEN = (0, 255, 0)

# Создаем из контуров прямоугольники, чтобы в последствии сделать фотки


def makeRects(contours):
    rects = []
    for cnt in contours:
        rect = cv.minAreaRect(cnt)  # пытаемся вписать прямоугольник
        rects.append(rect)
    return rects


def makeBoxes(rects):
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

    minDel = 1 if getMinS() < 1 else getMinS()
    maxDel = 1 if getMaxS() < 1 else getMaxS()

    s_min = W * H / minDel
    s_max = W * H * maxDel

    for crect in crects:

        box = cv.boxPoints(crect[0])  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат

        rect = crect[0]
        w, h = rect[1]
        if w == 0 or h == 0:
            continue
        deviation = 1 if getDeviation() < 1 else getDeviation()
        if w/h > deviation or h/w > deviation:
            continue
        s = crect[0][1][0] * crect[0][1][1]

        if s > s_min and s < s_max and crect[0][2] % 10 == 0:
            crect[1] = GREEN

    return crects


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


counter = 0


def save(crects, loadPath, savePath):
    global counter
    img = cv.imread(loadPath, 0)
    for crect in crects:
        color = crect[1]
        if canShow(color) == False:
            continue
        counter += 1
        rect = crect[0]
        box = cv.boxPoints(rect)  # поиск четырех вершин прямоугольника
        box = np.int0(box)  # округление координат
        x = int(rect[0][0])
        y = int(rect[0][1])
        w = int(rect[1][0])
        h = int(rect[1][1])
        path = "/home/artyom/labs/bauman/1/vkr/Good/"+str(counter)+".png"
        crop_img = img[y:y+h, x:x+w]
        cv.imwrite(path, crop_img)

# cv.getTrackbarPos("kernel size", "settings"))


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


def saveMeta(settings_file):
    to_json = {json_minV: getMinBrightness(),
               json_maxV: getMaxBrightness(),
               json_ksize: getKernelSize(),
               json_showB: getShowBlue(),
               json_showG: getShowGreen(),
               json_showR: getShowRed(),
               json_origin: getOrigin(),
               json_thresh: getThresh(),
               json_contours: getCountours(),
               json_min_s: int(10*getMinS()),
               json_max_s: int(10*getMaxS()),
               json_deviation: int(10*getDeviation()),
               json_ktype: getKtype()}
    f = open(settings_file, "w")
    f.write(json.dumps(to_json))


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


imagesPath = "/home/artyom/labs/bauman/1/vkr/Best"
savingPath = "/home/artyom/labs/bauman/1/vkr/Good"
configsPath = "/home/artyom/labs/bauman/1/vkr/output/configuration"

if __name__ == "__main__":
    print(__doc__)

    onlyfiles = [f for f in listdir(imagesPath) if isfile(join(imagesPath, f))]
    index = 0

    pathImage = imagesPath + "/" + onlyfiles[index]
    pathConfig = configsPath + "/"+str(index)

    if os.path.exists(pathConfig):
        loadMeta(pathConfig)

    while True:
        frame = cv.imread(pathImage, 0)
        drawOrigin(frame)

        # kernel is used to control the amount of eroding and dilating
        # kernel2 = np.ones((5, 5), np.uint8)  # np.uint8
        # kernel1 = np.array(
        #     [
        #         [0, 0, 1, 0, 0],
        #         [0, 1, 1, 1, 0],
        #         [1, 1, 1, 1, 1],
        #         [0, 1, 1, 1, 0],
        #         [0, 0, 1, 0, 0],
        #     ],
        #     dtype=np.uint8,
        # )
        # kernel = np.array([[0, 1, 0], [1, 1, 1], [0, 1, 0],], dtype=np.uint8,)
        # blur = cv.blur(frame,(5,5))
        ksize = getKernelSize()
        kernel = usualKernel(ksize)
        if getKtype() == 0:
            kernel = krestKernel(ksize)
        elif getKtype() == 2:
            kernel = rombKernel(ksize)
        print("kernel")
        print(kernel)
        img_erode = cv.erode(frame, kernel, iterations=2)
        # img_erode2 = cv.erode(frame, kernel1, iterations=2)
        # img_erode3 = cv.erode(frame, kernel2, iterations=2)
        # img_dilate = cv.dilate(frame, kernel, iterations=2)
        # cv.imshow("img_dilate1", img_erode)
        # cv.imshow("img_dilate2", img_erode2)
        # cv.imshow("img_erode", img_erode)
        # img_erode3 = img_erode

        # # Repeat the erosion and dilation by changing iterations.
        # img_erode = cv.erode(frame, kernel, iterations=2)
        # cv.imshow("img_erode", img_erode)

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
        thresh = cv.inRange(hsv, hsv_min, hsv_max)  # применяем цветовой фильтр
        drawThresh(thresh)

        # cv.imshow("thresh", thresh)
        img_dilate = cv.dilate(img_erode, kernel, iterations=2)

        contours0, hierarchy = cv.findContours(
            thresh, cv.RETR_TREE, cv.CHAIN_APPROX_SIMPLE
        )

        new_img2 = cv.imread(pathImage)
        drawCountours(new_img2, contours0)

        # path = os.path.join("/home/artyom/labs/bauman/vkr/output/2", "1.jpg")
        # cv.imwrite(path, thresh)

        ch = cv.waitKey(5)
        print("ch", ch)  # 13 81 83

        if ch == 49:
            if index > 0:
                saveMeta(pathConfig)
                index -= 1
                cv.destroyWindow(pathImage)
                pathImage = imagesPath + "/" + onlyfiles[index]
                pathConfig = configsPath + "/"+str(index)
                if os.path.exists(pathConfig):
                    loadMeta(pathConfig)

        if ch == 50:
            if index < len(onlyfiles) - 1:
                saveMeta(pathConfig)
                index += 1
                cv.destroyWindow(pathImage)
                pathImage = imagesPath + "/" + onlyfiles[index]
                pathConfig = configsPath + "/"+str(index)
                if os.path.exists(pathConfig):
                    loadMeta(pathConfig)

        if ch == 51:
            if inc + getMinBrightness() - 1 > 0:
                inc -= 1

        if ch == 52:
            if inc + getMinBrightness() + 1 < 255:
                inc += 1

        rects = makeRects(contours0)
        boxes = makeBoxes(rects)
        crects = markAll(rects)
        crects = markBlue(crects)
        crects = markGreen(crects)
        rimg = cv.imread(pathImage)
        draw(pathImage, rimg, boxes, crects)

        if ch == 13:
            save(crects, pathImage, savingPath)

        if ch == 27:
            break

    saveMeta(pathConfig)
    cv.destroyAllWindows()
