python3 cont_ex.py
opencv_createsamples -info /home/artyom/labs/bauman/1/vkr/Good.dat -vec samples.vec -w 20 -h 20 -num 4000
opencv_traincascade -data haarcascade6 -vec samples.vec -bg /home/artyom/labs/bauman/1/vkr/Bad.dat -numStages 21 -minhitrate 0.999 -maxFalseAlarmRate 0.4 -numPos 3500 -numNeg 4565 -w 20 -h 20 -mode ALL -precalcValBufSize 6096 -precalcIdxBufSize 6096


opencv_createsamples
-vec some_name - имя выходного файла
-img some_name - имя входного файла 
-num - количество фоток, которые надо создать
-w -h - ширина высота выходных выборок


opencv_traincascade
-data где хранится обученный классификатор
-acceptanceRatioBreakValue 0.0001
-numPos количество положительных фотографий
-numNeg количество негативных фотографий
-numStages колчество каскадных ступеней, подлежащиж обучению