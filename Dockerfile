FROM archlinux:latest AS build
COPY --chown=root:root . /home/gradle/src

WORKDIR /home/gradle/src


RUN pacman -Sy
RUN pacman -S --noconfirm gcc
RUN pacman -S --noconfirm make git
RUN pacman -S --noconfirm python
RUN pacman -S --noconfirm jdk11-openjdk gradle

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk

RUN make

RUN gradle build --no-daemon
