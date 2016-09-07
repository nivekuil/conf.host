#!/usr/bin/env python3
from bottle import post, request, run as bottle_run, HTTPResponse
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

    print(username)

    # TODO: Actual input validation and error handling
    if len(username) < 2:
        return "Username must be at least two characters long."
    if not len(pubkey):
        return "You should really include a public key."
    if username == "root":
        return "Nice try."

    useradd = run(["useradd", "--shell", shell, "-g", groupname,
                   "-d", home, username])
    if useradd.returncode == 9:
        return HTTPResponse(status=409,
                            body="Sorry, but that user already exists.")

    with open(authorized_keys, 'a+') as f:
        f.write(pubkey)

    chown(authorized_keys, username, groupname)
    chmod(authorized_keys, 0o600)

    return "Registered " + username

if __name__ == "__main__":
    bottle_run(host='0.0.0.0', port=5000)
