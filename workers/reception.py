#!/usr/bin/env python3
from flask import Flask, request
from subprocess import run
from os import mkdir, chmod
from shutil import chown

app = Flask(__name__)
shell = "/usr/bin/rssh"
home = "/var/local/confhost/airlock/"
groupname = "users"

@app.route("/", methods=['POST'])
def post():
    username = request.form.get('username')
    pubkey = request.form.get('pubkey')
    authorized_keys = "/etc/ssh/authorized_keys/" + username

    # TODO: Actual input validation and error handling
    if len(username) < 2 or len(pubkey) < 10:
        return
    if username == "root":
        return

    try:
        run(["useradd", "--shell", shell, "-g", groupname,
             "-d", home + username, username])
        # Make the home dir, separately so skel files don't get added
        mkdir(home + username)
        chown(home + username, user=username, group=groupname)
        # mkdir(ssh_dir, 0o700)
        # chown(ssh_dir, user=username, group=groupname)
        # Home dir must be owned by root for sshd chroot
        # chown(home + username, user="root", group="root")
    except Exception as e:
        # Check if file exists
        if e.errno == 17:
            print("User exists, adding key to authorized_keys.")
        else: print(e)

    with open(authorized_keys, 'a+') as f:
        f.write(pubkey)

    chown(authorized_keys, username, groupname)
    chmod(authorized_keys, 0o600)

    return request.data

if __name__ == "__main__":
    app.run(host='0.0.0.0')
