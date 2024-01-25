qemu-system-arm \
-kernel /Users/jonesjp1/dev/qemu-rpi-kernel/kernel-qemu-4.4.34-jessie \
-cpu cortex-a7 \
-m 256M \
-M raspi2 \
-serial stdio \
-append "rw earlyprintk root=/dev/sda2 rootfstype=ext4" \
-drive file=/Users/jonesjp1/dev/qemu_vms/orig/2018-04-18-raspbian-stretch.img,format=raw,index=0,media=disk \
-no-reboot

/Users/jonesjp1/dev/qemu-4.0.0/arm-softmmu/qemu-system-arm \
-kernel /Users/jonesjp1/dev/kernels/0828/kernel7.img \
-M raspi2 \
-cpu cortex-a7 \
-m 1G \
-serial stdio \
-append "rw root=/dev/mmcblk0p2 rootfstype=ext4 panic=1 console=ttyAMA0,115200 rootwait" \
-drive file=/Users/jonesjp1/dev/qemu_vms/2016-09-23-raspbian-jessie.img,format=raw,index=0,media=disk \
-no-reboot \
-nic \
-net user,hostfwd=tcp::5522-:22 \
-dtb /Users/jonesjp1/dev/dts/0828/bcm2709-rpi-2-b.dtb

-net tap,ifname=tap0,script=no,downscript=no \

-chardev socket,host=localhost,port=5522,server,nowait,id=port1-char \
-net user,hostfwd=tcp::5522-:22 \
-net nic \

-netdev user,id=ethernet.0,hostfwd=tcp::5522-:22 \
-device rtl8139,netdev=ethernet.0 \

/Users/jonesjp1/dev/qemu-4.0.0/arm-softmmu/qemu-system-arm \
-kernel /Users/jonesjp1/dev/kernels/0828/kernel7.img \
-M raspi2 \
-cpu cortex-a7 \
-m 1G \
-serial stdio \
-append "rw root=/dev/mmcblk0p2 rootfstype=ext4 panic=1 rootwait" \
-drive file=/Users/jonesjp1/dev/qemu_vms/2016-09-23-raspbian-jessie.img,format=raw,index=0,media=disk \
-no-reboot -usb \
-dtb /Users/jonesjp1/dev/dts/0828/bcm2709-rpi-2-b.dtb


-nic user,hostfwd=tcp::5022-:22


qemu-system-aarch64 \
-kernel /Users/jonesjp1/dev/kernels/0827/zImage \
-M raspi2 \
-m 1G \
-serial stdio \
-append "rw earlyprintk root=/dev/mmcblk0p2 rootfstype=ext4 console=ttyAMA0,115200 rootwait" \
-drive file=/Users/jonesjp1/dev/qemu_vms/2019-07-10-raspbian-buster-lite.img,format=raw,index=0,media=disk \
-no-reboot \
-dtb /Users/jonesjp1/dev/dts/bcm2709-rpi-2-b.dtb \
-monitor pty
