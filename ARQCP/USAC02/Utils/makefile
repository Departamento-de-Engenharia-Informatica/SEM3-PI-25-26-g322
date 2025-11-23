CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

TARGET = program.elf
OBJS = main.o decrypt_data.o callfunc.o

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJS) -o $(TARGET)

main.o: main.c
	$(CC) $(CFLAGS) $(ARCHFLAGS) -I../../Utils -c main.c -o main.o

decrypt_data.o: decrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c decrypt_data.s -o decrypt_data.o

callfunc.o: ../../Utils/callfunc.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../../Utils/callfunc.s -o callfunc.o

clean:
	rm -f *.o *.elf

run: $(TARGET)
	qemu-riscv32 -L $(LIBRV32) ./$(TARGET)

debug: $(TARGET)
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb -tui ./$(TARGET)"
	qemu-riscv32 -L $(LIBRV32) -g 1234 ./$(TARGET) -S
