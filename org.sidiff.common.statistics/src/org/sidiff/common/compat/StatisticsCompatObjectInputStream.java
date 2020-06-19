package org.sidiff.common.compat;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.sidiff.common.experiments.ExperimentalUtil;
import org.sidiff.common.statistics.StatisticsUtil;

/**
 * An object input stream used for compatibility with old serialized instances
 * of the {@link StatisticsUtil} and {@link ExperimentalUtil}. This correctly
 * resolves the classes of old serialized instances of statistics.
 * @author rmueller
 */
public final class StatisticsCompatObjectInputStream extends ObjectInputStream {

	public StatisticsCompatObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		ObjectStreamClass defaultDesc = super.readClassDescriptor();
		switch(defaultDesc.getName()) {
			case "org.sidiff.common.experimentalutil.ExperimentalUtil":
				return ObjectStreamClass.lookup(ExperimentalUtil.class);
			case "org.sidiff.common.util.StatisticsUtil":
				return ObjectStreamClass.lookup(StatisticsUtil.class);
		}
		return defaultDesc;
	}
}
