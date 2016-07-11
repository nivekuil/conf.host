#!/usr/bin/env python3
from bottle import post, request, run as bottle_run
from subprocess import run
from os import chmod
from shutil import chown

shell = "/usr/bin/rssh"
home = "/var/local/confhost/airlock"
groupname = "users"


@post("/")
def post():
    username = request.forms.get('username')
    pubkey = request.forms.get('pubkey')
    authorized_keys = "/etc/ssh/authorized_keys/" + username

    # TODO: Actual input validation and error handling
    if len(username) < 2 or len(pubkey) < 10:
        return "404"
    if username == "root":
        return "404"

    try:
        run(["useradd", "--shell", shell, "-g", groupname,
             "-d", home, username])
    except Exception as e:
        # Check if file exists
        if e.errno == 17:
            return "ERROR: That user already exists."
        else:
            return e

    with open(authorized_keys, 'a+') as f:
        f.write(pubkey)

    chown(authorized_keys, username, groupname)
    chmod(authorized_keys, 0o600)

    return request.data

if __name__ == "__main__":
    bottle_run(host='0.0.0.0', port=5000)
