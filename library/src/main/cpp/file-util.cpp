#include "file-util.h"

/**
 * 文件是否存在
 * @param filepath
 * @return
 */
bool file_exist(const char *filepath) {
    return access(filepath, F_OK) == 0;
}

/**
 * 打开文件
 * @param filepath
 * @return
 */
int file_open(const char *filepath) {
    return open(filepath, O_RDWR);
}

/**
 * 创建并打开文件
 * @param filepath
 * @return
 */
int file_create_and_open(const char *filepath) {
    return open(filepath, O_RDWR | O_CREAT);
}

/**
 * 读取文件
 * @param filepath
 * @return
 */
char* file_read(const char *filepath) {
    FILE *file = fopen(filepath, "r");
    if (!file) {
        return NULL;
    }
    int c;
    int i = 0;
    while(!feof(file)) {
        c = fgetc(file);
        if (c == 0 || c == -1) {
            break;
        }
        i++;
    }
    if (i == 0) {
        return NULL;
    }
    fseek(file, 0, SEEK_SET);
    char *result = (char*) malloc(i);
    char *buffer = (char*) malloc(i);
    int count = 0;
    size_t read = 0;
    while (!feof(file)) {
        // fgets遇到\n换行符会直接返回
        // fgets相当于readLine
        fgets(buffer, i, file);
        read = strlen(buffer);
        memcpy(result + count, buffer, read);
        count += read;
        if (count == i) {
            break;
        }
    }
    free(buffer);
    fclose(file);
    return result;
}

/**
 * 为map整理文件
 * 由于{@link file_map}函数的offset参数为4k的整数倍
 * 所以在文件中有效字节之间会存在很多为0字节(无效字节,由{@link ftruncate}函数生成)
 * 这里整理文件就是去掉这些无效字符 将有效字节连接起来
 * 计算下一个offset并返回
 * @param filepath
 * @return
 */
int file_make_for_map(const char *filepath) {
    // 以读的方式打开文件 读取以前的日志
    FILE *file = fopen(filepath, "r");
    if (!file) {
        return -1;
    }
    int size = 1024;
    int length = size / sizeof(int);
    int *cs = (int *) malloc(size);
    int i = 0;
    int c;
    while(!feof(file)) {
        c = fgetc(file);
        if (c == 0 || c == -1) {
            continue;
        }
        cs[i] = c;
        i++;
        if (i == length) {
            size += size;
            length = size / sizeof(int);
            cs = (int *) realloc(cs, size);
        }
    }
    fclose(file);
    if (i == 0) {
        return 0;
    }
    // 以写的方式再次打开文件 写入以前的文件
    file = fopen(filepath, "w");
    if (!file) {
        return -1;
    }
    for (int j = 0; j < i; j++) {
        fputc(cs[j], file);
    }
    free(cs);
    fclose(file);
    // 计算下一个映射位置 必须是4k的倍数
    size = i;
    if (size % 4096 != 0) {
        size = ((size / 4096) + 1) * 4096;
    }
    return size;
}

/**
 * 将内存映射到文件中
 * {@link mmap}之前一定调用{@link ftruncate}设置文件大小，以保证文件大小足够映射
 * @param fd
 * @param size
 * @param offset 一定是4k的整数倍 否则map失败
 * @return map映射的内存首地址 操作map的内存就可以映射到文件中
 */
char* file_map(int fd, size_t size, int offset) {
    // 注意map之前一定要调整文件大小 即使是空文件也要调整
    // 否则map虽然成功 但是操作不了map的内存
    ftruncate(fd, size);
    return (char *) mmap(0, size - offset, PROT_WRITE | PROT_READ, MAP_SHARED, fd, offset);
}

/**
 * 解除内存和文件的映射
 * @param address map返回的首地址
 * @param size 映射的大小
 */
int file_unmap(char* address, size_t size) {
    return munmap(address, size);
}
