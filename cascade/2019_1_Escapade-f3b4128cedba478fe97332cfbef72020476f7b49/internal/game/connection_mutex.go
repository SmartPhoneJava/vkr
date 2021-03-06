package game

import (
	"time"

	"github.com/go-park-mail-ru/2019_1_Escapade/internal/config"
	"github.com/gorilla/websocket"
)

// checkAndSetCleared checks if the cleanup function was called. This check is
// based on 'done'. If it is true, then the function has already been called.
// If not, set done to True and return false.
// IMPORTANT: this function must only be called in the cleanup function
func (conn *Connection) checkAndSetCleared() bool {
	conn.doneM.Lock()
	defer conn.doneM.Unlock()
	if conn._done {
		return true
	}
	conn._done = true
	return false
}

// setDone set done = true. It will finish all operaions on Connection
func (conn *Connection) setDone() {
	conn.doneM.Lock()
	conn._done = true
	conn.doneM.Unlock()
}

// done return '_done' field
func (conn *Connection) done() bool {
	conn.doneM.RLock()
	v := conn._done
	conn.doneM.RUnlock()
	return v
}

// Disconnected return   '_disconnected' field
func (conn *Connection) Disconnected() bool {
	if conn.done() {
		return conn._disconnected
	}
	conn.wGroup.Add(1)
	defer func() {
		conn.wGroup.Done()
	}()

	conn.disconnectedM.RLock()
	v := conn._disconnected
	conn.disconnectedM.RUnlock()
	return v
}

// setDisconnected set _disconnected true
func (conn *Connection) setDisconnected() {
	conn.disconnectedM.Lock()
	conn._disconnected = true
	conn.disconnectedM.Unlock()
	//conn.time = time.Now()
}

// SetConnected set_disconnected false and update last connection time
func (conn *Connection) SetConnected() {
	conn.disconnectedM.Lock()
	conn._disconnected = false
	conn.disconnectedM.Unlock()

	conn.timeM.Lock()
	conn._time = time.Now()
	conn.timeM.Unlock()
}

// Time return the last time the connection sent a 'pong' message
func (conn *Connection) Time() time.Time {
	conn.timeM.RLock()
	defer conn.timeM.RUnlock()
	return conn._time
}

// PlayingRoom return the pointer to the room in which the
// connection is playing
func (conn *Connection) PlayingRoom() *Room {
	if conn.done() {
		return conn._playingRoom
	}
	conn.wGroup.Add(1)
	defer func() {
		conn.wGroup.Done()
	}()

	conn.playingRoomM.RLock()
	v := conn._playingRoom
	conn.playingRoomM.RUnlock()
	return v
}

// WaitingRoom return the pointer to the room in which the
// connection is waiting other players to connect
func (conn *Connection) WaitingRoom() *Room {
	if conn.done() {
		return conn._playingRoom
	}
	conn.wGroup.Add(1)
	defer func() {
		conn.wGroup.Done()
	}()

	conn.waitingRoomM.RLock()
	v := conn._waitingRoom
	conn.waitingRoomM.RUnlock()
	return v
}

// Index return   '_index' field
func (conn *Connection) Index() int {
	if conn.done() {
		return conn._index
	}
	conn.wGroup.Add(1)
	defer func() {
		conn.wGroup.Done()
	}()

	conn.indexM.RLock()
	v := conn._index
	conn.indexM.RUnlock()
	return v
}

// SetIndex set '_index' - index in slice of players
func (conn *Connection) SetIndex(value int) {
	if conn.done() {
		return
	}
	conn.wGroup.Add(1)
	defer func() {
		conn.wGroup.Done()
	}()

	conn.indexM.Lock()
	conn._index = value
	conn.indexM.Unlock()
}

// setRoom set a pointer to the room in which the connection is located
func (conn *Connection) setPlayingRoom(room *Room) {
	conn.playingRoomM.Lock()
	conn._playingRoom = room
	conn.playingRoomM.Unlock()
}

// setBoth sets the flag whether the connection belongs to both the lobby and the room
func (conn *Connection) setWaitingRoom(room *Room) {
	conn.waitingRoomM.Lock()
	conn._waitingRoom = room
	conn.waitingRoomM.Unlock()
}

// websocket

func (conn *Connection) wsInit(wsc config.WebSocketSettings) {
	conn.wsM.Lock()
	conn._ws.SetReadLimit(wsc.MaxMessageSize)
	conn._ws.SetReadDeadline(time.Now().Add(wsc.PongWait))
	conn._ws.SetPongHandler(
		func(string) error {
			conn._ws.SetReadDeadline(time.Now().Add(wsc.PongWait))
			conn.SetConnected()
			return nil
		})
	conn.wsM.Unlock()
}

func (conn *Connection) wsReadMessage() (messageType int, p []byte, err error) {
	//conn.wsM.Lock()
	//defer conn.wsM.Unlock()
	messageType, p, err = conn._ws.ReadMessage()
	return
}

func (conn *Connection) wsWriteMessage(mt int, payload []byte, wsc config.WebSocketSettings) error {
	conn.wsM.Lock()
	defer conn.wsM.Unlock()
	conn._ws.SetWriteDeadline(time.Now().Add(wsc.WriteWait))
	return conn._ws.WriteMessage(mt, payload)
}

func (conn *Connection) wsClose() error {
	conn.wsM.Lock()
	defer conn.wsM.Unlock()
	return conn._ws.Close()
}

func (conn *Connection) wsWriteInWriter(message []byte, wsc config.WebSocketSettings) error {
	conn.wsM.Lock()
	conn._ws.SetWriteDeadline(time.Now().Add(wsc.WriteWait))
	w, err := conn._ws.NextWriter(websocket.TextMessage)
	conn.wsM.Unlock()
	if err != nil {
		return err
	}
	w.Write(message)

	return w.Close()
}
