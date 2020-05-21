package game

import (
	"sync"

	"github.com/go-park-mail-ru/2019_1_Escapade/internal/config"
	"github.com/go-park-mail-ru/2019_1_Escapade/internal/models"
	"github.com/go-park-mail-ru/2019_1_Escapade/internal/utils"

	"math"
	"math/rand"
	"time"
)

// Field send to user, if he disconnect and 'forgot' everything
// about map or it is his first connect
type Field struct {
	wGroup *sync.WaitGroup

	doneM *sync.RWMutex
	_done bool

	matrixM *sync.RWMutex
	_matrix [][]int32

	historyM *sync.RWMutex
	_history []*Cell
	Width    int32
	Height   int32

	cellsLeftM *sync.RWMutex
	_cellsLeft int32

	config *config.Field

	Mines     int32
	Difficult float64
}

// NewField create new instance of field
func NewField(rs *models.RoomSettings, config *config.Field) *Field {
	matrix := generate(rs)
	field := &Field{
		wGroup: &sync.WaitGroup{},

		doneM: &sync.RWMutex{},
		_done: false,

		matrixM: &sync.RWMutex{},
		_matrix: matrix,

		historyM: &sync.RWMutex{},
		_history: make([]*Cell, 0, rs.Width*rs.Height),
		Width:    rs.Width,
		Height:   rs.Height,
		Mines:    rs.Mines,

		config: config,

		cellsLeftM: &sync.RWMutex{},
	}
	var cellsleft int32
	if rs.Deathmatch {
		cellsleft = rs.Width*rs.Height - rs.Mines - rs.Players
	} else {
		cellsleft = rs.Width*rs.Height - rs.Mines
	}
	field.setCellsLeft(cellsleft)
	field.Difficult = float64(field.Mines) / float64(field._cellsLeft)
	return field
}

// Free clear matrix and history
func (field *Field) Free(timeout time.Duration) {

	if field.checkAndSetCleared() {
		return
	}

	utils.WaitWithTimeout(field.wGroup, timeout)

	go field.matrixFree()
	go field.historyFree()
}

// SameAs compare two fields
func (field *Field) SameAs(another *Field) bool {
	field.wGroup.Add(1)
	defer field.wGroup.Done()

	left1 := field.cellsLeft()
	left2 := field.cellsLeft()

	compare := field.Width == another.Width &&
		field.Height == another.Height &&
		left1 == left2

	return compare
}

// OpenEverything open all cells
func (field *Field) OpenEverything(cells *[]Cell) {
	if field.Done() {
		return
	}
	field.wGroup.Add(1)
	defer field.wGroup.Done()

	var i, j, v int32
	for i = 0; i < field.Height; i++ {
		for j = 0; j < field.Width; j++ {
			v = field.matrixValue(i, j)
			if v != CellOpened && v != CellFlagTaken {
				cell := NewCell(i, j, v, 0)
				field.saveCell(cell, cells)
				field.setCellOpen(i, j, v)
			}
		}
	}
}

// openCellArea open cell area, if there is no mines around
// in this cell
func (field *Field) openCellArea(x, y, ID int32, cells *[]Cell) {
	if field.areCoordinatesRight(x, y) {
		v := field.matrixValue(x, y)

		if v < CellMine {
			cell := NewCell(x, y, v, ID)
			field.saveCell(cell, cells)
			field.decrementCellsLeft()
		}
		if v == 0 {
			field.openCellArea(x-1, y-1, ID, cells)
			field.openCellArea(x-1, y, ID, cells)
			field.openCellArea(x-1, y+1, ID, cells)

			field.openCellArea(x, y+1, ID, cells)
			field.openCellArea(x, y-1, ID, cells)

			field.openCellArea(x+1, y-1, ID, cells)
			field.openCellArea(x+1, y, ID, cells)
			field.openCellArea(x+1, y+1, ID, cells)
		}
	}
}

// IsCleared return true if all safe cells except flags open
func (field *Field) IsCleared() bool {
	return field.cellsLeft() == 0
}

// saveCell save cell to the slice 'cells' and to the slice of
// opened cells
func (field *Field) saveCell(cell *Cell, cells *[]Cell) {
	if cell.Value != CellOpened && cell.Value != CellFlagTaken {
		cell.Time = time.Now()
		field.setToHistory(cell)
		*cells = append(*cells, *cell)
		field.setCellOpen(cell.X, cell.Y, cell.Value)
	}
}

// OpenCell open 'cell' and return slice of opened cells
func (field *Field) OpenCell(cell *Cell) (cells []Cell) {
	if field.Done() {
		return
	}
	field.wGroup.Add(1)
	defer field.wGroup.Done()

	cell.Value = field.matrixValue(cell.X, cell.Y)

	utils.Debug(false, "!!!!!!!!!!!!!!!11cell.Value", cell.Value)

	cells = make([]Cell, 0)
	if cell.Value < CellMine {
		field.openCellArea(cell.X, cell.Y, cell.PlayerID, &cells)
	} else {
		if cell.Value != FlagID(cell.PlayerID) {
			field.saveCell(cell, &cells)
		}
	}

	return
}

// RandomFlags create random players flags
func (field *Field) RandomFlags(players []Player) (flags []Flag) {
	if field.Done() {
		return
	}
	field.wGroup.Add(1)
	defer field.wGroup.Done()

	flags = make([]Flag, len(players))
	for i, player := range players {
		flags[i] = Flag{
			Cell: field.CreateRandomFlag(player.ID),
			Set:  false,
		}
	}
	return flags
}

// CreateRandomFlag create flag for player
func (field *Field) CreateRandomFlag(playerID int32) (cell Cell) {
	if field.Done() {
		return
	}
	field.wGroup.Add(1)
	defer field.wGroup.Done()

	rand.Seed(time.Now().UnixNano())
	var x, y int32
	x = rand.Int31n(field.Width)
	y = rand.Int31n(field.Height)
	cell = *NewCell(x, y, FlagID(playerID), playerID)

	return cell
}

// OpenSave open n(or more) cells that do not contain any mines or flags
func (field *Field) OpenSave(n int) (cells []Cell) {
	cells = make([]Cell, 0)
	size := 0

	for n > size {
		rand.Seed(time.Now().UnixNano())
		i := rand.Int31n(field.Width)
		j := rand.Int31n(field.Height)
		if field.lessThenMine(i, j) {
			cells = append(cells, field.OpenCell(NewCell(i, j, 0, 0))...)
			size = len(cells)
		}
	}
	return
}

// OpenZero open all cells around which there are no mines, or which are
// located next to the cells in which there is no mine
func (field *Field) OpenZero() (cells []Cell) {
	cells = make([]Cell, 0)

	var i, j int32
	for i = 0; i < field.Width; i++ {
		for j = 0; j < field.Height; j++ {
			if field.matrixValue(i, j) == 0 {
				cells = append(cells, field.OpenCell(NewCell(i, j, 0, 0))...)
			}
		}
	}
	return
}

// Zero clears the entire matrix of values
func (field *Field) Zero() {
	var i, j int32
	for i = 0; i < field.Width; i++ {
		for j = 0; j < field.Height; j++ {
			field.setMatrixValue(i, j, 0)
		}
	}
}

// SetMines fill matrix with mines
func (field *Field) SetMines(flags []Flag, deathmatch bool) {
	if field.Done() {
		return
	}
	field.wGroup.Add(1)
	defer field.wGroup.Done()

	var (
		width  = field.Width
		height = field.Height
		mines  = field.Mines
	)

	if deathmatch {
		var (
			gip      = float64(field.Width*field.Width) + float64(field.Height*field.Height)
			areaSize = gip / field.Difficult / 2000.0

			minAreaSize = float64(field.config.MinAreaSize)
			maxAreaSize = float64(field.config.MaxAreaSize)
		)

		if areaSize < minAreaSize {
			areaSize = minAreaSize
		} else if areaSize > maxAreaSize {
			areaSize = maxAreaSize
		}
		var (
			areaSizeINT    = int32(areaSize)
			probability    = 5 * int(areaSize*areaSize)
			minProbability = field.config.MinProbability
			maxProbability = field.config.MaxProbability
		)
		if probability > maxProbability {
			probability = maxProbability
		} else if probability < minProbability {
			probability = minProbability
		}
		utils.Debug(false, "we have %d flags, area size %f; probability %d;;;;%f\n", len(flags), gip/field.Difficult/2000.0, probability, field.Difficult)

		for _, flag := range flags {
			x := flag.Cell.X
			y := flag.Cell.Y
			utils.Debug(false, "flag[%d, %d] - %d\n", x, y, flag.Cell.PlayerID)
			var i, j int32
			for i = x - areaSizeINT; i <= x+areaSizeINT; i++ {
				if i >= 0 && i < width {
					for j = y - areaSizeINT; j <= y+areaSizeINT; j++ {
						if j >= 0 && j < height {
							if field.matrixValue(i, j) != CellMine && !(x == i && y == j) {
								rand.Seed(time.Now().UnixNano())
								procent := rand.Intn(100)
								utils.Debug(false, "[%d, %d] - %d\n", i, j, procent)
								if procent > probability {
									field.setMatrixValue(i, j, CellMine)
									mines--
									if mines == 0 {
										return
									}
								}
							}
						}
					}
				}
			}
		}
	}
	for mines > 0 {

		rand.Seed(time.Now().UnixNano())
		someX := rand.Int31n(width)
		someY := rand.Int31n(height)

		if field.lessThenMine(someX, someY) {
			field.setMatrixValue(someX, someY, CellMine)
			mines--
		}
	}
}

// SetMinesCounters set the counters of min - cells,
// which are not mine or flags
func (field *Field) SetMinesCounters() {

	var width, height, x, y, i, j, value int32
	width = field.Width
	height = field.Height

	for x = 0; x < width; x++ {
		for y = 0; y < height; y++ {
			if field.matrixValue(x, y) != 0 {
				continue
			}
			value = 0

			for i = x - 1; i <= x+1; i++ {
				if i >= 0 && i < width {
					for j = y - 1; j <= y+1; j++ {
						if j >= 0 && j < height {
							if field.matrixValue(i, j) == CellMine {
								value++
							}
						}
					}
				}
			}
			field.setMatrixValue(x, y, value)
		}
	}
}

// generate matrix
func generate(rs *models.RoomSettings) (matrix [][]int32) {
	width := rs.Width
	height := rs.Height

	matrix = [][]int32{}
	for i := int32(0); i < height; i++ {
		matrix = append(matrix, make([]int32, width))
	}
	return
}

// IsInside check if coordinates are in field
func (field Field) areCoordinatesRight(x, y int32) bool {
	return x >= 0 && x < field.Width && y >= 0 && y < field.Height
}

// IsInside check is cell inside fueld
func (field Field) IsInside(cell *Cell) bool {
	return field.areCoordinatesRight(cell.X, cell.Y)
}

// FlagID convert player ID to Flag ID
func FlagID(connID int32) int32 {
	return int32(math.Abs(float64(connID))) + CellIncrement
}

///////////////////// Set cells func //////////

// SetFlag works only when mines not set
func (field *Field) SetFlag(cell *Cell) {
	if field.Done() {
		return
	}
	field.wGroup.Add(1)
	defer field.wGroup.Done()

	// To identifier which flag we see, lets set id
	// add CellIncrement to id, because if id = 3 we can think that there are 3 mines around
	// we cant use -id, becase in future there will be a lot of conditions with
	// something < 9 (to find not mine places)
	utils.Debug(false, "setFlag", cell.X, cell.Y, cell.PlayerID, CellIncrement)

	field.setMatrixValue(cell.X, cell.Y, FlagID(cell.PlayerID))
}

// setCellOpen set cell opened
func (field *Field) setCellOpen(x, y, v int32) {
	if v < CellMine {
		field.setMatrixValue(x, y, CellOpened)
	} else {
		field.setMatrixValue(x, y, CellFlagTaken)
	}
}
