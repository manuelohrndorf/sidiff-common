package org.sidiff.common.emf.metrics.testdriver;

import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.common.emf.metrics.MetricsFacade;
import org.sidiff.common.emf.metrics.MetricsList;
import org.sidiff.common.emf.modelstorage.SiDiffResourceSet;
import org.sidiff.common.statistics.StatisticsUtil;

public class MetricTestdriverApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] modelNames = {
				"test_smarthome.ecore",
				"test_smarthome.uml",
				"superimposed.sim",
				"test.deltamodel",
		};

		SiDiffResourceSet resourceSet = SiDiffResourceSet.create();
		for(String modelName : modelNames) {
			URI uri = URI.createPlatformPluginURI("/org.sidiff.common.emf.metrics.testdriver/models/" + modelName, true);
			Resource resource = resourceSet.getResource(uri, true);

			System.out.println("########### Metrics : " + uri + " ###########");
			MetricsList metrics = MetricsFacade.getMetrics(resource);
			metrics.recomputeAll(new NullProgressMonitor());
			System.out.println("All metrics: " + metrics);
			metrics.removeAllIrrelevant();
			System.out.println("Irrelevant metrics removed: " + metrics);
			Map<String,Object> data = metrics.export();
			System.out.println("Metrics data: " + data);
			StatisticsUtil statUtil = StatisticsUtil.createStatisticsUtil(data);
			System.out.println("StatisticsUtil: " + statUtil);
			System.out.println("CSV:");
			System.out.println(metrics.exportAsCsv());
			System.out.println();
		}
		return null;
	}

	@Override
	public void stop() {
	}
}
