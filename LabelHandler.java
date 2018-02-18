

import java.util.HashMap;


public class LabelHandler {
    static int counter; // All label handlers share the same counter.
    LabelHandler parent; // Allows you to look at the parent scope.
    HashMap<String, String> identifiers; // Variable and function names.
    String name; // The name of this scope.

    LabelHandler() {
        this.identifiers = new HashMap<>();
    }

    // Create a new handler without a parent.
    // This is handy for the global scope.
    LabelHandler(String n) {
        this();
        this.name = n;
    }

    // Create a sub-scope.
    LabelHandler(LabelHandler p, String n) {
        this();
        this.parent = p;
        this.name = n;
    }

    // Given the name, return the associated label.
    // This recursively walks up the scope levels, checking parent scopes.
    public String getLabel(String n) {
        String label = identifiers.get(n);
        if(label == null && this.parent != null)
            label = parent.getLabel(n);
        return label; // If the label doesn't exist in the scope, it returns null.
    }

    // Check to see if the label exists in this scope only.
    // If you decide to allow identical labels if sub scopes, you can use
    // this check. If you want to limit identical labels in sub-scopes, use
    // labelExistsAll.
    public boolean labelExists(String n) {
        return identifiers.containsKey(n);
    }

    // Check label all the way up the scope.
    public boolean labelExistsAll(String n) {
        if(!identifiers.containsKey(n) && this.parent!= null)
            return parent.labelExistsAll(n);
        return identifiers.containsKey(n);
    }

    // Check for the label's existence first. If you don't, this will overwrite
    // the old label.
    // Returns the new label.
    public String addLabel(String n) {
        this.identifiers.put(n, "L"+LabelHandler.counter++);
        return this.getLabel(n);
    }

    // Make a new sub-scope with the parent set to this scope.
    public LabelHandler newScope(String n) {
        return new LabelHandler(this, n);
    }
}
