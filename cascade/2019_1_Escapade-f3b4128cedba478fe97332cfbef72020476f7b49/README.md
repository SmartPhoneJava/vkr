# Escapade [![Go Report Card](https://goreportcard.com/badge/github.com/go-park-mail-ru/2019_1_Escapade)](https://goreportcard.com/report/github.com/go-park-mail-ru/2019_1_Escapade) [![Build Status](https://travis-ci.org/go-park-mail-ru/2019_1_Escapade.svg?branch=develop)](https://travis-ci.org/go-park-mail-ru/2019_1_Escapade) [![godoc](http://img.shields.io/badge/godoc-reference-blue.svg?style=flat)](https://godoc.org/github.com/go-park-mail-ru/2019_1_Escapade)

:heart_eyes: Top backend :heart_eyes:

- [Играть](https://explosion.team)
- [Резервный сайт](https://ser.ru.com/)
- [Репозиторий фронтенда](https://github.com/frontend-park-mail-ru/2019_1_Escapade)
- [Дизайн и концепты](https://www.figma.com/file/WcFryEu51iySsuBd8F0CLi0S/explose)

## Правила игры:
Все знают, как играть в сапера, поэтому описывать singleplayer смысла нет, опишу особенности multiplayer.
- Концепция: каждый игрок ставит флаг. Задача найти флаги других игроков, избегая мин. При открытии безопасных ячеек вам начисляются баллы. При обнаружении флага соперника, вы получаете МНОГО очков! Но будьте осторожны, при подрыве на мине вы теряете существенную долю очков и вам сразу присуждается поражение. Можно вообще не сражаться, а ждать, пока ваши соперники подорвуться на мине, ну не чудесно ли? Как бы то ни было, побеждает выживший, чей флаг не найден и набравший наибольшее количество очков.
- Игра проходит в два этапа: подготовка и игра. 
  - Во время подготовки вам дается около 10 секунд на установку флага. Если вы не установите флаг, он будет выбран автоматически. Установить флаг можно сколько угодно раз, но только на протяжении этапа подготовки, в дальнейшем поменять его расположение уже не получится! Если вы поставите флаг в ячейку, в котором находится другой флаг, оба флага переместятся в случайные места. Подготовка завершается либо по таймеру, либо когда все игроки поставили флаг. И не бойтесь, ячейка с флагом не может находиться на ячейке с миной, поэтому на вашем флаге никто не подорвется. :)
  - Второй этап длится определенное время. Увидеть оставшееся время можно на таймере сверху-справа. Открытые игроком ячейки видны всем в комнате. Однако на правую кнопку мыши вы можете ставить флажки(для обозначения мин). Их будете видеть только вы.
- Игра завершается в случае достижения одной из следующий ситуаций:
  - вышло время;
  - все игроки(кроме одного) подорвались или их флаги были обнаружены;
  - все игроки(кроме одного) вышли или разорвали подключение;
- Чтобы начать игру, надо создать или подключиться к уже созданной комнате. Если вы подключиться к комнате, в которой идет набор игроков, то вы подключитесь в роли игрока. А при попытке подключиться к уже запущенной комнате, вы станете наблюдателем. Вы будете видеть ход игры и сможете общаться с другими наблюдателями и игроками в чате комнаты.

## Локальный запуск:
- sudo apt  install docker-compose
- sudo docker-compose up -d
- Enjoy!

P.S. Пользовательские аватарки не будут отображаться, поскольку для доступа к фотографиям мы используем AWS ключи, которые в репозитории не содержатся

## Состав:
- Надежный ментор - [Дмитрий Липко](https://github.com/dlipko)
- Неспящий бэкэндер - [Артём Доктор](https://github.com/SmartPhoneJava)
- Креативный фронтендер - [Иван Спасенов](https://github.com/slevinsps)
- Усердный фронтендер - [Сергей Апарин](https://github.com/Bigyin1)