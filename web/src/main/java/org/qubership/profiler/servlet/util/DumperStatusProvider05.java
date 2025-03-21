package org.qubership.profiler.servlet.util;

import org.qubership.profiler.agent.Bootstrap;
import org.qubership.profiler.agent.DumperPlugin;
import org.qubership.profiler.agent.DumperPlugin_05;

public class DumperStatusProvider05 extends DumperStatusProvider04 {
    DumperPlugin_05 dumper = (DumperPlugin_05) Bootstrap.getPlugin(DumperPlugin.class);

    @Override
    public void update() {
        super.update();
        fileRead = dumper.getFileRead();
        writtenBytes = dumper.getFileWritten();
    }
}
