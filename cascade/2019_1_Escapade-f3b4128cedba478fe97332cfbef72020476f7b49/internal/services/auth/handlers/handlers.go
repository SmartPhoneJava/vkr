package handlers

import (
	"encoding/json"
	"net/http"
	"net/url"
	"os"

	"github.com/go-park-mail-ru/2019_1_Escapade/internal/utils"
	"github.com/go-session/session"
	pg "github.com/vgarvardt/go-oauth2-pg"
	"gopkg.in/oauth2.v3/server"
)

func deleteHandler(srv *server.Server, tokenStore *pg.TokenStore) func(w http.ResponseWriter, r *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		utils.Debug(false, "/delete")
		token, err := srv.ValidationBearerToken(r)
		if err != nil {
			utils.Debug(false, "error", err.Error())
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}
		if err = tokenStore.RemoveByAccess(token.GetAccess()); err != nil {
			utils.Debug(false, "error", err.Error())
			http.Error(w, err.Error(), http.StatusBadRequest)
		}

		e := json.NewEncoder(w)
		e.Encode(token)
	}
}

func testHandler(srv *server.Server) func(w http.ResponseWriter, r *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		utils.Debug(false, "/test")
		token, err := srv.ValidationBearerToken(r)
		if err != nil {
			utils.Debug(false, "error", err.Error())
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}
		utils.Debug(false, "token", token.GetAccessExpiresIn())

		e := json.NewEncoder(w)
		e.Encode(token)
		utils.Debug(false, "/dont know")
	}
}

func tokenHandler(srv *server.Server) func(w http.ResponseWriter, r *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		utils.Debug(false, "/token")
		err := srv.HandleTokenRequest(w, r)
		if err != nil {
			utils.Debug(false, "cant@", err.Error())
			http.Error(w, err.Error(), http.StatusInternalServerError)
		}
	}
}

func authorizeHandler(srv *server.Server) func(w http.ResponseWriter, r *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		utils.Debug(false, "/authorize")
		store, err := session.Start(nil, w, r)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		var form url.Values
		if v, ok := store.Get("ReturnUri"); ok {
			form = v.(url.Values)
		}
		r.Form = form

		store.Delete("ReturnUri")
		store.Save()

		err = srv.HandleAuthorizeRequest(w, r)
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
		}
	}
}

func loginHandler(w http.ResponseWriter, r *http.Request) {
	utils.Debug(false, "/loginHandler")
	store, err := session.Start(nil, w, r)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	if r.Method == "POST" {
		store.Set("LoggedInUserID", "000000")
		store.Save()

		w.Header().Set("Location", "/auth")
		w.WriteHeader(http.StatusFound)
		return
	}
	outputHTML(w, r, "static/login.html")
}

func authHandler(w http.ResponseWriter, r *http.Request) {
	utils.Debug(false, "/authHandler")
	store, err := session.Start(nil, w, r)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	if _, ok := store.Get("LoggedInUserID"); !ok {
		w.Header().Set("Location", "/login")
		w.WriteHeader(http.StatusFound)
		return
	}

	outputHTML(w, r, "static/auth.html")
}

func outputHTML(w http.ResponseWriter, req *http.Request, filename string) {
	file, err := os.Open(filename)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	defer file.Close()
	fi, _ := file.Stat()
	http.ServeContent(w, req, file.Name(), fi.ModTime(), file)
}
