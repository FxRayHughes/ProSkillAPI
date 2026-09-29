package com.sucy.skill.dynamic;

import com.rit.sucy.config.parse.DataSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks named shared subtrees before any roots subscribe to events. A group
 * remains a v1 root, and every run-group call is a normal mechanic child.
 */
public final class GroupScanner {
    private GroupScanner() { }

    public static void scan(String skill, DataSection components) {
        if (components == null) return;
        Map<String, DataSection> groups = new HashMap<>();
        for (String key : components.keys()) {
            if (!key.replaceAll("-.+", "").equalsIgnoreCase("GROUP")) continue;
            DataSection root = components.getSection(key);
            DataSection data = root == null ? null : root.getSection("data");
            String name = data == null ? "" : data.getString("group", "").trim();
            if (name.isEmpty() || groups.putIfAbsent(name, root) != null)
                throw new IllegalArgumentException("Missing or duplicate group in " + skill + ": " + name);
        }
        Map<String, List<String>> calls = new HashMap<>();
        for (Map.Entry<String, DataSection> entry : groups.entrySet()) {
            List<String> refs = new ArrayList<>();
            collect(entry.getValue().getSection("children"), refs, 0);
            calls.put(entry.getKey(), refs);
        }
        List<String> outside = new ArrayList<>();
        for (String key : components.keys()) if (!key.replaceAll("-.+", "").equalsIgnoreCase("GROUP")) {
            DataSection root = components.getSection(key);
            if (root != null) collect(root.getSection("children"), outside, 0);
        }
        for (String ref : outside) if (!groups.containsKey(ref))
            throw new IllegalArgumentException("Unknown group in " + skill + ": " + ref);
        for (String group : groups.keySet()) visit(group, calls, groups.keySet(), new HashSet<>(), new HashSet<>());
    }

    private static void collect(DataSection children, List<String> refs, int depth) {
        if (children == null) return;
        if (depth > 100) throw new IllegalArgumentException("Group tree exceeds 100 levels");
        for (String key : children.keys()) {
            DataSection node = children.getSection(key);
            if (node == null) continue;
            if (key.replaceAll("-.+", "").equalsIgnoreCase("run group")) {
                DataSection data = node.getSection("data");
                String ref = data == null ? "" : data.getString("group", "").trim();
                if (ref.isEmpty()) throw new IllegalArgumentException("Run group has no group key");
                refs.add(ref);
            }
            collect(node.getSection("children"), refs, depth + 1);
        }
    }

    private static void visit(String group, Map<String, List<String>> calls, Set<String> known,
                              Set<String> visiting, Set<String> done) {
        if (done.contains(group)) return;
        if (!visiting.add(group)) throw new IllegalArgumentException("Recursive group call: " + group);
        for (String ref : calls.get(group)) {
            if (!known.contains(ref)) throw new IllegalArgumentException("Unknown group: " + ref);
            visit(ref, calls, known, visiting, done);
        }
        visiting.remove(group);
        done.add(group);
    }
}
