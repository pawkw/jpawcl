# jpawcl

This is currently a very rudimentary expression parser. It asks the user to input an expression on the command line and then prints out an s-expression and an Intel compatible assembly language "compile".

Anything the expression parser does not understand is left unparsed to be picked up by parent stages of the compiler (which don't exist yet).

## Requirements

This compiles with jdk 8 and 9.

## Build

To build jpawcl use: `javac *.java`.

To run it use: `java pawcl`.

Example:
```
Enter an expression to parse, enter a blank line to end.
The only variable declared is 'a'.
Expression: a&3*8%5
Parsed expression as: (& a (% (* 3 8) 5))
Unparsed remainder:
mov rax, 3
imul rax,8
idiv 5
mov rax, rdx
and rax,[a]

Enter an expression to parse, enter a blank line to end.
The only variable declared is 'a'.
Expression: a*5%8 >= 4
Parsed expression as: (% (* a 5) 8)
Unparsed remainder: >= 4
mov rax, [a]
imul rax,5
idiv 8
mov rax, rdx

Enter an expression to parse, enter a blank line to end.
The only variable declared is 'a'.
Expression:
```
