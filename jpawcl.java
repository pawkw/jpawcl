/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Peter Weston
 */
class ParseNode {
    String operator; // The operator or "identifier" or "literal".
    String data;   // The identifier or literal.
    ParseNode lhs; // Left hand tree.
    ParseNode rhs; // Right hand tree.


    ParseNode() {}

    ParseNode(String op, String data, ParseNode left, ParseNode right) {
        this.operator = op;
        this.data = data;
        this.lhs = left;
        this.rhs = right;
    }

    public void set(String op, String data, ParseNode left, ParseNode right) {
        this.operator = op;
        this.data = data;
        this.lhs = left;
        this.rhs = right;
    }

    @Override
    public String toString() {
        if(this.operator.equals("literal") || this.operator.equals("identifier"))
            return this.data;
        if(this.operator.equals("neg"))
            return "(neg "+this.lhs+")";
        if(this.operator.equals("~"))
            return "(not "+this.lhs+")";
        return "("+this.operator+" "+this.lhs+" "+this.rhs+")";
    }

    public SimpleList assemble(SourceHandler source) {
        // Check to see if this is a literal or identifier.
        if(this.operator.equals("literal"))
            return new SimpleList("mov rax, "+this.data);
        if(this.operator.equals("identifier"))
            return new SimpleList("mov rax, ["+this.data+"]");

        // Assign the assembly language code.
        String op;
        switch(this.operator) {
            case "+":
                op = "add";
                break;
            case "-":
                op = "sub";
                break;
            case "*":
                op = "imul"; // Signed multiplication.
                break;
            case "/":
                op = "idiv"; // Signed division. This op isn't passed along. See below.
                break;
            case "%":
                op = "mod"; // This is temporary.
                break;
            case "neg":
                op = "neg";
                break;
            case "~":
                op = "not";
                break;
            case "&":
                op = "and";
                break;
            case "|":
                op = "or";
                break;
            case "^":
                op = "xor";
                break;
            default:
                jpawcl.abort(source, "Error while assembling expression.");
                op = "";
        }
        // If the operator is +, *, &, |, ^ and the left side is a identifier or
        //  literal and the right side is not, swap sides.
        // It's messy here, but better assembly.
        switch(this.operator) {
            case "+":
            case "*":
            case "&":
            case "|":
            case "^":
                if(lhs.operator.equals("literal") || lhs.operator.equals("identifier"))
                    if(!"literal".equals(rhs.operator) && !"identifier".equals(rhs.operator)) {
                        ParseNode temp;
                        temp = this.rhs;
                        this.rhs = this.lhs;
                        this.lhs = temp;
                    }
                break;
        }

        // Do the left hand side.
        SimpleList left = this.lhs.assemble(source);

        // Handle unary minus and not.
        if(op.equals("neg")) {
            left.add("neg rax");
            return left;
        }
        if(op.equals("not")) {
            left.add("not rax");
            return left;
        }

        SimpleList right;

        // If the right side is an identifier or literal.
        if("identifier".equals(this.rhs.operator)) {
            if(op.equals("mod")){
                right = new SimpleList("idiv ["+this.rhs.data+"]");
                right.add("mov rax, rdx");
            } else
                right = new SimpleList(op+" rax,["+this.rhs.data+"]");
        }
        else if("literal".equals(this.rhs.operator))
            if(op.equals("mod")) {
                right = new SimpleList("idiv "+this.rhs.data);
                right.add("mov rax, rdx");
            } else
                right = new SimpleList(op+" rax,"+this.rhs.data);
        else {
            if(op.equals("idiv") || op.equals("mod")) {
                // This is messy here, but better than swapping rax and rbx later.
                right = this.rhs.assemble(source);
                right.add("push rax");
                right.add(left);
                right.add("pop rbx");
                right.add("xor rdx, rdx"); // rdx is the upper qword of the dividend.
                right.add("idiv rbx"); // rax is the implicit dividend.
                if(op.equals("mod"))
                    right.add("mov rax, rdx"); // Move the remainder into rax.
                return right;
            }
            right = new SimpleList("push rax");
            right.add(rhs.assemble(source));
            right.add("pop rbx");
            right.add(op+" rax, rbx");
        }
        left.add(right);
        return left;

    }
}

class SimpleList {
    Object data;
    SimpleList next;

    SimpleList() {

    }

    SimpleList(Object o) {
        this.data = o;
    }

    SimpleList(Object o, SimpleList list) {
        this.data = o;
        this.next = list;
    }

    public void add(Object o) {
        if(this.next == null)
            this.next = new SimpleList(o);
        else
            this.next.add(o);
    }

    public void add(SimpleList list) {
        if(this.next == null)
            this.next = list;
        else
            this.next.add(list);
    }

    public void print() {
        SimpleList temp;
        temp = this;
        do {
            System.out.println(temp.data);
            if(temp.next == null)
                break;
            temp = temp.next;
        } while(true);
    }
}

public class jpawcl {
    static SimpleList operators;

    public static void main(String[] args) {
        // Added in reverse order of precedence.
        // Brackets, unary minus, not, literals and identifiers are handled
        //   in the factor method.
        operators = new SimpleList("|"); // Bitwise or.
        operators.add("^"); // Bitwise xor.
        operators.add("&"); // Bitwise and.
        operators.add("+-");
        operators.add("*/%");

        // Get the filename from args.

        // Load the file into the source handler.

        // Init
        // Set up expression node, code handler.

        ParseNode node;
        SimpleList assembly;
        Scanner userInput = new Scanner(System.in);
        String input = new String();
        SourceHandler source;
        LabelHandler labels;
        labels = new LabelHandler("global");
        labels.addLabel("a");

        do {
            System.out.println("Enter an expression to parse, enter a blank line to end.");
            System.out.println("The only variable declared is 'a'.");
            System.out.print("Expression: ");
            input = userInput.nextLine();
            if(input.equals(""))
                break;
            source = new SourceHandler(input);
            node = expression(source, labels, operators);
            System.out.println("Parsed expression as: "+node);
            System.out.println("Unparsed remainder: "+source.rest());
            assembly = node.assemble(source);
            assembly.print();
            System.out.println();
        } while(true);
    }

    // Print out an error and then exit.
    public static void abort(SourceHandler source, String s){
        source.error();
        System.out.println(s);
        System.exit(1);
    }

    private static ParseNode expression(SourceHandler source, LabelHandler labels, SimpleList ops) {
        ParseNode node;
        source.depad();
        // System.out.println("exression: "+source.rest());
        if(ops == null) // Empty list.
            return factor(source, labels);
        node = expression(source, labels, ops.next);
        String operator;
        while(ops.data.toString().contains(""+source.peek)) {// Casting to a string doesn't work(?).
            operator = ""+source.peek;
            source.match(operator);
            node = new ParseNode(operator, "", node, expression(source, labels, ops.next));
        }
        return node;
    }
    // Parse a factor node.
    // This also parses the terms.
    private static ParseNode factor(SourceHandler source, LabelHandler labels) {
        ParseNode node = new ParseNode();
        source.depad();

        if(source.peek == '(') {
            source.match("(");
            // Start from scratch. Brackets can contain full expressions.
            node = expression(source, labels, operators);
            source.match(")");
        } else if (source.peek == '-') { // Unary minus.
            source.match("-");
            // Usining "neg" so the assembler doesn't get confused.
            // Factor is called here. If you are negating a larger expression,
            //   it has to be in brackets.
            node.set("neg", "", factor(source, labels), null);
        } else if(source.peek == '~') { // Not operator.
            source.match("~");
            node.set("~", "", factor(source, labels), null);
        } else {
            // Check the terms.
            String id = source.getIdentifier();
            String in = source.getInteger();

            if(id != null) {
                // Check to see if the identifier has been declared.
                if(!labels.labelExists(id))
                    abort(source, "Identifier not found. Check scope, spelling, etc.");
                source.match(id);
                node.set("identifier", id, null, null);
            } else if(in != null) {
                // Integers are the only thing handled currently.
                source.match(in);
                node.set("literal", in, null, null);
            } else
                abort(source, "Parse error.");
        }
        return node;
    }
}
