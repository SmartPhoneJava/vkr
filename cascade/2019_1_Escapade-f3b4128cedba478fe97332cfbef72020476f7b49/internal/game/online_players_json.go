package game

import (
	"sync"
)

// Flag contaion Cell and flag, that it was set by User
//easyjson:json
type Flag struct {
	Cell Cell `json:"cell"`
	Set  bool `json:"set"`
}

// OnlinePlayers online players
type OnlinePlayers struct {
	capacityM *sync.RWMutex
	_capacity int32

	playersM *sync.RWMutex
	_players []Player

	flagsM      *sync.RWMutex
	_flags      []Flag
	flagsLeft   int32
	Connections *Connections
}

// OnlinePlayersJSON is a wrapper for sending OnlinePlayers by JSON
//easyjson:json
type OnlinePlayersJSON struct {
	Capacity    int32           `json:"capacity"`
	Players     []Player        `json:"players"`
	Connections ConnectionsJSON `json:"connections"`
	Flags       []Flag          `json:"flags"`
}

// JSON convert OnlinePlayers to OnlinePlayersJSON
func (op *OnlinePlayers) JSON() OnlinePlayersJSON {
	return OnlinePlayersJSON{
		Capacity:    op.Capacity(),
		Players:     op.CopyPlayers(),
		Connections: op.Connections.JSON(),
		Flags:       op.Flags(),
	}
}

// MarshalJSON - overriding the standard method json.Marshal
func (op *OnlinePlayers) MarshalJSON() ([]byte, error) {
	return op.JSON().MarshalJSON()
}

// UnmarshalJSON - overriding the standard method json.Unmarshal
func (op *OnlinePlayers) UnmarshalJSON(b []byte) error {
	temp := &OnlinePlayersJSON{}

	if err := temp.UnmarshalJSON(b); err != nil {
		return err
	}

	op.SetCapacity(temp.Capacity)
	op.SetPlayers(temp.Players)
	op.SetFlags(temp.Flags)

	return nil
}

// NewConnections create instance of Connections
func newOnlinePlayers(size int32, field Field) *OnlinePlayers {
	players := make([]Player, size)
	flags := make([]Flag, size)
	return &OnlinePlayers{
		capacityM: &sync.RWMutex{},
		_capacity: size,

		playersM: &sync.RWMutex{},
		_players: players,

		flagsM:      &sync.RWMutex{},
		_flags:      flags,
		flagsLeft:   size,
		Connections: NewConnections(size),
	}

}
