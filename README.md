docker run -i -t <image_name> /bin/bash

<image_name> from line:
Successfully built 9d5f4f8485e6

e.g. docker run --add-host=fruitfly_gcs:0.0.0.0 -i -t 9d5f4f8485e6 /bin/bash

List all docker images:
docker images -a

OpenSSL Debugging
openssl s_client -host fruitfly-gcs.localhost -port 8000 -cert /etc/ssl/certs/server-cert.pem -key /etc/ssl/certs/server-key.pem

self-signed certificate problems:
(service) config set "strict-ssl" false -g
For example:
yarn config set "strict-ssl" false -g
node config set "strict-ssl" false -g

Path to safari cache of website data:
Macintosh HD⁩ ▸ ⁨Users⁩ ▸ ⁨jonesjp1⁩ ▸ ⁨Library⁩ ▸ ⁨Containers⁩ ▸ ⁨com.apple.Safari⁩ ▸ ⁨Data⁩ ▸ ⁨Library⁩ ▸ ⁨Caches⁩ ▸ ⁨com.apple.Safari⁩ ▸ ⁨WebKitCache⁩ ▸ ⁨Version 14⁩ ▸ ⁨Records⁩ ▸ ⁨B0EC1A3BCCD9799AA6EDC7FD02766753A4B89F59⁩ ▸ ⁨Resource⁩

For CI/CD
Found the actual "real" images here:
https://hub.docker.com/r/gitlab/gitlab-runner-helper/tags?page=1
Then went to tags (should be selected from the URL)
Then filtered the tags.
I found that the URL that had been trying to be pulled:
gitlab/gitlab-runner-helper:x86_64-1564076bf

was actually
gitlab/gitlab-runner-helper:x86_64-1564076b

In the future, just filter the tags by the first part of the tag. For example,
"x86_64-1564"
and you should be able to find the right image. Once you have the right image,
edit the config.toml file found here: ~/.gitlab-runner/config.toml
Specifically, this line:
helper_image = "gitlab/gitlab-runner-helper:x86_64-1564076b"
And make it the proper tag for the image

To run the test-runner locally:
(ensure there's a symbolic link to the .gitlab-ci.yml file in the base directory
of the repo)
$ gitlab-runner exec docker job1
