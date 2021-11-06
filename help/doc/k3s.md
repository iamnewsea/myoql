# k3s

## nfs
```
yum install nfs-utils
systemctl start nfs-server
mkdir -p /data/nfs

vi /etc/exports
/data/nfs *(rw)

systemctl restart nfs-server
showmount -e localhost
```