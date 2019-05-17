//
// Created by Administrator on 2019/5/13.
//

#ifndef LOGGER_FILE_UTIL_H
#define LOGGER_FILE_UTIL_H

#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

bool file_exist(const char *filepath);

int file_open(const char *filepath);

int file_create_and_open(const char *filepath);

char* file_read(const char *filepath);

int file_make_for_map(const char *filepath);

char* file_map(int fd, size_t size, int offset);

int file_unmap(char* address, size_t size);

#endif //LOGGER_FILE_UTIL_H
