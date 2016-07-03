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
es = Elasticsearch(
    hosts = ['127.0.0.1:9200'],
)
airlock = "/var/local/confhost/airlock/"
path_len = len(airlock.split(os.path.sep))
index_name = "resource"         # Elasticsearch index to insert resources into

def push():
    Timer(0.5, push).start()
    for entry in os.scandir(airlock):
        # TODO: Handle directories
        if entry.is_dir():
            continue

        stat = entry.stat()
        # Get username by looking at file owner
        username = pwd.getpwuid(stat.st_uid).pw_name
        # Full path of the file
        resource = entry.path

        # If fuser has any output, then the file is currently being used
        # by another process.  This likely means that is is still being
        # transferred, so skip it for now.
        if run(["fuser", resource], stdout=PIPE).stdout:
            continue

        if len(username) == 0:
            raise ValueError("Username cannot be empty.")

        # Add file to IPFS
        resource_hash = ipfs.add(resource)[0]["Hash"]
        print("Adding to IPFS " + resource_hash)

        body = {
            'owner': username,
            'filename': entry.name,
            'filesize': stat.st_size,
            'last_modified': stat.st_mtime,
            'contents': None,
        }
        with open(resource) as f:
            body["contents"] = f.read()

        es.index(
            index = index_name,
            doc_type = username,
            id = resource_hash,
            body = body
        )

        os.remove(resource)

        # Remove only empty directories, i.e. username directories after
        # all internal files have been uploaded to the cluster
        # try: os.rmdir(root)
        # except: continue

if __name__ == "__main__":
    push()
