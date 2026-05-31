set -e

docker volume create portainer_data

docker volume create registry_data

docker run -d \
  --name portainer \
  --restart=always \
  -p 9000:9443 \
  -p 8000:8000 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v portainer_data:/data \
  portainer/portainer-ce:latest

docker run -d \
  --name registry \
  --restart=always \
  -p 5000:5000 \
  -v registry_data:/var/lib/registry \
  registry:2
