all:
	git submodule update --recursive --init
	cd java-tree-sitter && ./build.py
docker:
	cd java-tree-sitter && ./build.py
