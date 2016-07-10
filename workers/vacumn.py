#!/usr/bin/env python3
"""
Worker that runs on an scp endpoint, sending files from the airlock to the
interplanetary file system (and Elasticsearch).
"""
import os
import pwd
from subprocess import run, PIPE
from threading import Timer
from ipfsApi import Client
from elasticsearch import Elasticsearch

ipfs = Client('127.0.0.1', 5001)
es = Elasticsearch(hosts=['127.0.0.1:9200'])
airlock = "/var/local/confhost/airlock/"
path_len = len(airlock.split(os.path.sep))
index_name = "resource"         # Elasticsearch index to insert resources into


def push():
    Timer(1, push).start()
    for entry in os.scandir(airlock):

        resource = entry.path
        stat = entry.stat()

        # If fuser has any output, then the file is currently being used
        # by another process.  This likely means that is is still being
        # transferred, so skip it for now.
        if run(["fuser", resource], stdout=PIPE).stdout:
            continue

        # Get username by looking at file owner
        username = pwd.getpwuid(stat.st_uid).pw_name
        # TODO: input validation
        if len(username) == 0:
            raise ValueError("Username cannot be empty.")

        # add_to_hashlist(resource_hash, stat.st_mtime * 1000)
        # Only add/pin files < 1MB
        if stat.st_size < 1000000:
            resource_hash = ipfs.add(resource)[0]["Hash"]
            print("Adding to IPFS " + resource_hash)

        # TEMPORARY: Only add files, not directories to ES.
        if entry.is_file():
            add_to_elastic(resource, username, entry.name, stat.st_size,
                           entry.st_mtime)

        print("Ejecting ", resource)
        run(["rm", "-r", resource])


def add_to_elastic(path, ipfs_hash, username, filename, filesize, mtime):
    print("Adding to elasticsearch ", filename)
    body = {
        'owner': username,
        'filename': filename,
        'extension': filename.split(".")[-1],
        'filesize': filesize,
        'mtime': int(mtime * 1000),
        'contents': "",
    }

    # Only add file contents to Elasticsearch if < 100kB
    if filesize < 100000:
        with open(path) as f:
            body["contents"] = f.read()

    es.index(index=index_name, doc_type=username,
             id=ipfs_hash, body=body)


def add_to_hashlist(new_hash, time):
    # peer_id = ipfs.id()['ID']
    current_hashlist_hash = ipfs.name_resolve()['Path']
    current_hashlist = ipfs.cat(current_hashlist_hash).split('\n')
    # Append the new hash
    new_hashlist = current_hashlist.append(new_hash)
    # Store the old hashlist hash on the first line of the new hashlist
    # followed by a space and the epoch time (in ms)
    new_hashlist[0] = current_hashlist_hash + " " + time
    new_hashlist_hash = ipfs.add('\n'.join(new_hashlist))[0]["Hash"]
    ipfs.name_publish(new_hashlist_hash)

if __name__ == "__main__":
    push()
