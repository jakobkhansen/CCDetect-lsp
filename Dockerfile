FROM --platform=linux/amd64 archlinux:latest AS build
COPY --chown=root:root . /root/CCDetect-lsp

WORKDIR /root/CCDetect-lsp


RUN pacman -Sy
RUN pacman -S --noconfirm gcc
RUN pacman -S --noconfirm make git
RUN pacman -S --noconfirm python
RUN pacman -S --noconfirm jdk11-openjdk gradle
RUN pacman -S --noconfirm neovim

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk

RUN make

RUN gradle build --no-daemon

WORKDIR /root

run mkdir .dotfiles
RUN git --git-dir=$HOME/.dotfiles/ --work-tree=$HOME clone --bare https://github.com/jakobkhansen/CCDetect-nvim-config.git $HOME/.dotfiles 
RUN git --git-dir=$HOME/.dotfiles/ --work-tree=$HOME checkout
RUN git clone https://github.com/NASAWorldWind/WorldWindJava
