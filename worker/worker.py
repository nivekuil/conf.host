"""
Worker that runs on an scp server, feeding recieved files into Elasticsearch
and IPFS.
"""
import os
from ipfsApi import Client
from elasticsearch import Elasticsearch
from threading import Timer

ipfs = Client('127.0.0.1', 5001)
es = Elasticsearch(
    hosts = ['127.0.0.1:9200'],
)
file_dir = "/home/nivekuil/code/confhost/test/"

def push():
    Timer(0.5, push).start()
    for root, dirs, files in os.walk(file_dir):
        if root == file_dir: continue;
        for f in files:
            username = root.split("/")[-1] # Name of parent directory
            resource = root + "/" + f      # Name of the file

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
        os.rmdir(root)

if __name__ == "__main__":
    push()
