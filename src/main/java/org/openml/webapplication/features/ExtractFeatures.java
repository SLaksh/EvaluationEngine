/*
 *  Webapplication - Java library that runs on OpenML servers
 *  Copyright (C) 2014 
 *  @author Jan N. van Rijn (j.n.van.rijn@liacs.leidenuniv.nl)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.openml.webapplication.features;

import java.util.ArrayList;
import java.util.List;

import org.openml.apiconnector.algorithms.Conversion;
import org.openml.apiconnector.xml.DataFeature.Feature;
import org.openml.webapplication.models.AttributeStatistics;

import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instances;

public class ExtractFeatures {
	
	private static final int MAX_SIZE_CLASS_DISTR = 16384;
	
	public static List<Feature> getFeatures(Instances dataset, String defaultClass) throws Exception {
		if (defaultClass != null) {
			if(defaultClass.contains(",")){
				dataset.setClass(dataset.attribute(defaultClass.split(",")[0]));
			} else {
				dataset.setClass(dataset.attribute(defaultClass));
			}
		} else {
			dataset.setClassIndex(dataset.numAttributes()-1);
		}
		
		final ArrayList<Feature> resultFeatures = new ArrayList<Feature>();
		
		for (int i = 0; i < dataset.numAttributes(); i++) {
			Attribute att = dataset.attribute(i);
			int numValues = dataset.classAttribute().isNominal() ? dataset.classAttribute().numValues() : 0;
			AttributeStatistics attributeStats = new AttributeStatistics(dataset.attribute(i),numValues);
		
			for (int j = 0; j < dataset.numInstances(); ++j) {
				attributeStats.addValue(dataset.get(j).value(i), dataset.get(j).classValue());
			}
			
			String data_type = null;
			List<String> nominal_values = new ArrayList<>();
			
			Integer numberOfDistinctValues = null;
			Integer numberOfUniqueValues = null;
			Integer numberOfMissingValues = null;
			Integer numberOfIntegerValues = null;
			Integer numberOfRealValues = null;
			Integer numberOfNominalValues = null;
			Integer numberOfValues = null;
			
			Double maximumValue = null;
			Double minimumValue = null;
			Double meanValue = null;
			Double standardDeviation = null;
				
			AttributeStats as = dataset.attributeStats(i);
				
			numberOfDistinctValues = as.distinctCount;
			numberOfUniqueValues = as.uniqueCount;
			numberOfMissingValues = as.missingCount;
			numberOfIntegerValues = as.intCount;
			numberOfRealValues = as.realCount;
			numberOfMissingValues = as.missingCount;
			
			
			if (att.isNominal()) {
				numberOfNominalValues = att.numValues(); 
			}
			numberOfValues = attributeStats.getTotalObservations();
			
			if (att.isNumeric()) {
				maximumValue = attributeStats.getMaximum();
				minimumValue = attributeStats.getMinimum();
				meanValue = attributeStats.getMean();
				standardDeviation = 0.0;
				try {
					standardDeviation = attributeStats.getStandardDeviation();
				} catch(Exception e) {
					Conversion.log("WARNING", "StdDev", "Could not compute standard deviation of feature "+ att.name() +": "+e.getMessage());
				}
			}
			
			if (att.type() == Attribute.NUMERIC) {
				data_type = "numeric";
			} else if (att.type() == Attribute.NOMINAL) {
				data_type = "nominal";
				for (int j = 0; j < att.numValues(); ++j) {
					nominal_values.add(att.value(j));
				}
			} else if (att.type() == Attribute.STRING) {
				data_type = "string";
			} else if (att.type() == Attribute.DATE) {
				data_type = "date";
			} else {
				data_type = "unknown";
			}
			String classDistr = attributeStats.getClassDistribution();
			if (classDistr.length() > MAX_SIZE_CLASS_DISTR) {
				classDistr = null;
			}
			resultFeatures.add(new Feature(att.index(), att.name(), 
					data_type, nominal_values.toArray(new String[nominal_values.size()]),
					att.index() == dataset.classIndex(), 
					numberOfDistinctValues,
					numberOfUniqueValues, numberOfMissingValues,
					numberOfIntegerValues, numberOfRealValues,
					numberOfNominalValues, numberOfValues,
					maximumValue, minimumValue, meanValue,
					standardDeviation, classDistr));
		}
		return resultFeatures;
	}
}
