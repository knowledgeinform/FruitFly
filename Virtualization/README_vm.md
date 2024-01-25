qemu-system-arm \
-kernel /Users/jonesjp1/dev/qemu-rpi-kernel/kernel-qemu-4.4.34-jessie \
-cpu cortex-a7 \
-m 256M \
-M raspi2 \
-serial stdio \
-append "rw earlyprintk root=/dev/sda2 rootfstype=ext4" \
-drive file=/Users/jonesjp1/dev/qemu_vms/orig/2018-04-18-raspbian-stretch.img,format=raw,index=0,media=disk \
-no-reboot


/home/apluser/Downloads/qemu-4.0.0/arm-softmmu/qemu-system-arm \
-kernel /home/apluser/dev/linux/arch/arm/boot/zImage \
-M raspi2 \
-cpu cortex-a7 \
-m 1G \
-serial stdio \
-append "rw earlyprintk root=/dev/mmcblk0p2 rootfstype=ext4 panic=1 console=ttyAMA0,115200 fsck.repair=yes initcall_debug=1 rootwait" \
-drive file=/home/apluser/dev/images/2016-09-23-raspbian-jessie.img,format=raw,index=0,media=disk \
-no-reboot \
-nic \
-dtb /home/apluser/dev/linux/arch/arm/boot/dts/bcm2709-rpi-2-b.dtb

-net user,hostfwd=tcp::5522-:22 \

-netdev tap,ifname=tap0,script=no,downscript=no,id=my_net_id \
-device driver=virtio-net,netdev=my_net_id \

-net nic \
-net user,hostfwd=tcp::5522-:22 \


-nic user,hostfwd=tcp::5022-:22


qemu-system-aarch64 \
-kernel /home/apluser/dev/linux/arch/arm/boot/zImage \
-M raspi2 \
-m 1G \
-serial stdio \
-append "rw earlyprintk root=/dev/mmcblk0p2 rootfstype=ext4 console=ttyAMA0,115200 rootwait" \
-drive file=/home/apluser/dev/images/2019-07-10-raspbian-buster-lite.img,format=raw,index=0,media=disk \
-no-reboot \
-dtb /home/apluser/dev/linux/arch/arm/boot/dts/bcm2709-rpi-2-b.dtb \
-monitor pty
