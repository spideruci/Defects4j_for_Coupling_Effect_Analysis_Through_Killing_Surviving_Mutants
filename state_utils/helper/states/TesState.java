package org.helper.states;

import org.helper.Utils;
import org.helper.graph.Arguments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.helper.Utils.readLinesFromFileToSet;


/**
 * This class is used to store the states of the test execution
 */
public class TesState {

    public static List<StateItem> states = new ArrayList<StateItem>();

    public static Set<String> staticFields = new HashSet<String>();

    static {
        if (Utils.checkFileExist("initialize/staticFields.txt")) {
            staticFields = readLinesFromFileToSet("initialize/staticFields.txt");
        }
    }

    /**
     * Add a state to the list of states
     * the max size of the list is 2000
     * @param state
     */
    public synchronized static void addState(StateItem state) {
        if (states.size() < 2000 && org.helper.TesExtension.inRecording) {
            states.add(state);
            if (org.helper.TesExtension.dumpingTiming.contains(states.size())) {
//                states.add(new StateItem(new Loc("this", "this"), type.Object, "", TestExtension.thisClass));
                org.helper.TesExtension.dumpStates(org.helper.TesExtension.dir, org.helper.TesExtension.f, Arguments.depth, states.size());
//                states.remove(states.size() - 1);
            }
        }
    }

    public synchronized static void clear() {
        states.clear();
    }

    public synchronized static void prettyPrint() {
        for (StateItem state : states) {
            String output = "";
            output += state.getLine() + ": " + state.getSource() + " -> " + state.getObservers();
            System.out.println(output);
        }
    }
}

