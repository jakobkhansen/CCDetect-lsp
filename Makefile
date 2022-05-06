all:
	git submodule update --recursive --init
	cd java-tree-sitter && ./build.py
