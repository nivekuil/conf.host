#!/usr/bin/env python3
"""
Worker that runs on an scp endpoint, sending files from the airlock to the
interplanetary file system (and Elasticsearch).
"""
import os
from subprocess import run, PIPE
from ipfsApi import Client
from elasticsearch import Elasticsearch
from threading import Timer

ipfs = Client('127.0.0.1', 5001)
es = Elasticsearch(
    hosts = ['127.0.0.1:9200'],
)
airlock = "/var/local/confhost/airlock/"
path_len = len(airlock.split(os.path.sep))

def push():
    Timer(0.5, push).start()
    for root, dirs, files in os.walk(airlock):
        # Don't look for files in the root dir
        if root == airlock: continue
        for f in files:

            # Directory directly under airlock
            username = root.split(os.path.sep)[path_len-1]
            # Name of the file
            resource = os.path.join(root, f)

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
                'filesize': os.path.getsize(resource),
                'last_modified': os.path.getmtime(resource),
                'contents': open(resource).read(),
            }
            es.index(
                index = "resource",
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
