#!/usr/bin/env python
# -*- coding: utf-8 -*-
# vim:fileencoding=utf-8

import sys
import numpy as np
import cv2 as cv2
import os
import random
from datetime import datetime

import easygui
import unicodedata

import json

from os import listdir
from os.path import isfile, join

my_cascade = cv2.CascadeClassifier('cascade1.xml')
owl = cv2.CascadeClassifier('owl.xml')


imagesPath = "/home/artyom/labs/bauman/1/vkr/Best"
savingPath = "/home/artyom/labs/bauman/1/vkr/Good"
configsPath = "/home/artyom/labs/bauman/1/vkr/output/configuration"

if __name__ == "__main__":
    print(__doc__)

    onlyfiles = [f for f in listdir(imagesPath) if isfile(join(imagesPath, f))]
    index = 0

    cap = cv2.VideoCapture(0)
    cap.set(cv2.CAP_PROP_FPS, 24) # Чистота кадров
    while True:
        ret, frame = cap.read()
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

       
        # gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        faces = owl.detectMultiScale(
            gray,               #
            scaleFactor=1.2,    #
            minNeighbors=5,     #
            minSize=(20, 20)    #
        )

        for (x, y, w, h) in faces:
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 255), 2)
        cv2.imshow("camera", frame)
        if cv2.waitKey(10) == 27:  # Esc key
            break
        
    cap.release()
    cv.destroyAllWindows()
