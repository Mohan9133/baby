/*******************************************************************************
 *  Copyright (C) 2007, 2015:
 *  
 *    - Ferdinando Villa <ferdinando.villa@bc3research.org>
 *    - integratedmodelling.org
 *    - any other authors listed in @author annotations
 *
 *    All rights reserved. This file is part of the k.LAB software suite,
 *    meant to enable modular, collaborative, integrated 
 *    development of interoperable data and model components. For
 *    details, see http://integratedmodelling.org.
 *    
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the Affero General Public License 
 *    Version 3 or any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but without any warranty; without even the implied warranty of
 *    merchantability or fitness for a particular purpose.  See the
 *    Affero General Public License for more details.
 *  
 *     You should have received a copy of the Affero General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *     The license is also available at: https://www.gnu.org/licenses/agpl.html
 *******************************************************************************/
package 3000.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.integratedmodelling.api.modelling.IActiveDirectObservation;
import org.integratedmodelling.api.modelling.IActiveProcess;
import org.integratedmodelling.api.modelling.IModel;
import org.integratedmodelling.api.modelling.INumericObserver;
import org.integratedmodelling.api.modelling.IObservable;
import org.integratedmodelling.api.modelling.IObservation;
import org.integratedmodelling.api.modelling.IScale;
import org.integratedmodelling.api.modelling.IState;
import org.integratedmodelling.api.modelling.contextualization.IProcessContextualizer;
import org.integratedmodelling.api.modelling.contextualization.IStateContextualizer;
import org.integratedmodelling.api.modelling.resolution.IResolutionScope;
import org.integratedmodelling.api.modelling.scheduling.ITransition;
import org.integratedmodelling.api.monitoring.IMonitor;
import org.integratedmodelling.api.project.IProject;
import org.integratedmodelling.api.services.annotations.Prototype;
import org.integratedmodelling.api.space.ISpatialExtent;
import org.integratedmodelling.common.space.IGeometricShape;
import org.integratedmodelling.common.states.States;
import org.integratedmodelling.common.vocabulary.NS;
import org.integratedmodelling.exceptions.KlabException;
import org.integratedmodelling.exceptions.KlabValidationException;

import com.vividsolutions.jts.geom.Point;

/**
 * This class implements a process contextualizer. The @Prototype annotation,
 * along with its being in the component's package, registers the contextualizer
 * with the k.IM language so that it can be used to observe processes.
 * 
 * The interfaces are not mandatory for @Prototype but are needed for the
 * registration service to know what to do with the classes. This is an
 * IProcessContextualizer, so the correspondent function can be used in the
 * 'using' clause of a process model, linked to a process observable.
 * 
 * When the service is called the first time, initialize() will be called on a
 * freshly instantiated ExampleProcessContextualizer object, which will be then
 * kept around to handle any other transitions in the same model run. So it's
 * safe to store state in the object at initialize() to reuse later at
 * compute(). The service object will be disposed of when canDispose() returns
 * true - usually after the last transition has gone through compute(), although
 * implementations define that. This can be checked in compute() using
 * ITransition.isLast(). If the model calling this has no temporal extent,
 * compute() will never be called, and all the work is done by initialize(). The
 * latter should check if that's the case and set the return value for
 * canDispose() appropriately, otherwise the service will be kept in the session
 * unnecessarily until session termination or timeout.
 * 
 * The parameters to @Prototype define the public API for the function (and/or
 * the REST service connected to it). We need the return type to be this (it
 * needs to be in sync with the interfaces implemented; this is slightly
 * redundant but remains for now). The published parameter allows to control
 * visibility in a networked context; an optional list of groups to restrict it
 * to can also be given. Argument list follows the conventions described in
 * {@link org.integratedmodelling.api.services.annotations.Prototype}.
 * 
 * Here we provide a demonstrational service that will create any outputs by
 * summing the values of any inputs, all of which need to have numeric
 * observers. If a multiplier parameter is given, the process will also multiply
 * the outputs by it. Each transition will add a random value to the output,
 * chosen between zero and the multiplier (or 1 if not passed).
 * 
 * It's a process model so the semantics is for the "value summing" process;
 * this example could also be implemented with a quality model using a
 * {@link IStateContextualizer}. Process models are of course used for
 * observables that describe meaningful processes.
 * 
 * @author ferdinando.villa
 *
 */
@Prototype(
		id = "example.p", 
		returnTypes = { NS.PROCESS_CONTEXTUALIZER },
		// leave published to false or this will be advertised on all servers of the
		// network
		published = false, 
		args = { "? m|multiplier", Prototype.INT })
public class ExampleProcessContextualizer implements IProcessContextualizer {

	boolean canDispose = false;
	int multiplier = 1;
	IScale scale = null;
	Map<String, IObservation> outputStates = new HashMap<>();

	@Override
	public boolean canDispose() {
		return canDispose;
	}

	/**
	 * Called once on each object, at the beginning of contextualization,
	 * passing the process this contextualizer will be handling. The process
	 * contains its scale and the subject in which it's happening. It should
	 * check the available inputs for sufficiency (mostly enforced by the
	 * semantics, but we don't know what the modeler will write) and that we can
	 * produce the outputs. If not, we should throw a
	 * ThinklabValidationException.
	 * 
	 * This is also a good time to check the scale and the subject semantics and
	 * see if we are OK being contextualized in it. The semantic validation will
	 * have been done when we get here, but we may have additional requirements
	 * (although that's very bad practice).
	 * 
	 * If everything is OK, we should produce the initial values of our outputs
	 * as required, and return them. If we need to keep anything around -
	 * process, subject, scale etc - we should set fields here, as those won't
	 * be passed to compute().
	 * 
	 * NOTE: a process that changes the input will have the same observable in
	 * the outputs, with the same name (this is unimplemented for now). In that
	 * case, the state will be automatically created and will be dynamic,
	 * capable of being redefined at compute().
	 * 
	 * @return the initial values of any observations made.
	 */
	@Override
	public Map<String, IObservation> initialize(IActiveProcess process, IActiveDirectObservation context,
			IResolutionScope resolutionContext, Map<String, IObservable> expectedInputs,
			Map<String, IObservable> expectedOutputs, IMonitor monitor) throws KlabException {

		this.scale = process.getScale();

		/**
		 * Standard way of proceeding: we can dispose of the object at this
		 * stage only if the scale has no time or no timesteps. Being this a
		 * process model we could just assume that there is time, as processes
		 * only happen in time, but this is the standard way to check and it may
		 * catch some degenerate cases that may (?) have a rationale to exist.
		 */
		if (process.getScale().getTime() == null || process.getScale().getTime().getMultiplicity() <= 1) {
			canDispose = true;
		}

		/**
		 * Check the observers in the list of prospective inputs and outputs. We
		 * only get their semantics, as we don't know if there are values
		 * already computed for any of the inputs - there may be, but it depends
		 * on the contextualization strategy which can change at each run.
		 * 
		 * The strings passed in the inputs and output maps are their formal
		 * name within the model statement, and may change at each run although
		 * they will be stable during the lifetime of this object.
		 * 
		 * As these are inputs, we know for certain that if they are qualities,
		 * the observer for them will be in the observable, so we can check for
		 * that.
		 * 
		 * Because we are processes, which happen in the context of subjects and
		 * share the scale, we could check the subject (using getState()) to see
		 * if we have any of the inputs, as long as we don't depend on them
		 * being there.
		 */
		for (String inp : expectedInputs.keySet()) {

			/**
			 * The observable contains the different concepts that define the
			 * semantics of both the observable and the observation. In this
			 * context, we are also guaranteed that it will contain an observer
			 * whenever that's appropriate - i.e. when the observable is a
			 * quality (NS.isQuality(observable) == true).
			 * 
			 * In most real-life situations, there should be no need for this
			 * kind of check - the reasoner would validate the inputs before we
			 * get here.
			 * 
			 */
			IObservable o = expectedInputs.get(inp);
			if (o.getObserver() != null && !(o.getObserver() instanceof INumericObserver)) {
				throw new KlabValidationException("example.process: input state " + inp + " is not numeric");
			}
		}

		/*
		 * see if we have any inputs already available in the subject. Use
		 * getStates() - if getState(IObservable) is called, non-existing states
		 * would be created.
		 */
		IState inputState = null;
		if (context.getStates().size() > 0) {
			/*
			 * use the first dependency just to get some numbers. In a real-life
			 * contextualizer, we would most likely know the expected states by
			 * observable.
			 */
			inputState = context.getStates().iterator().next();
		}

		/*
		 * for each output, we ensure we are requested a numeric one, and we
		 * create an initial state with the value of the inputs if it's there,
		 * zero otherwise.
		 */
		for (String out : expectedOutputs.keySet()) {

			IObservable obs = expectedOutputs.get(out);

			/*
			 * same comments as before
			 */
			if (obs.getObserver() != null && !(obs.getObserver() instanceof INumericObserver)) {
				throw new KlabValidationException("example.process: output state " + out + " is not numeric");
			}

			/**
			 * Output states are not created directly but must be asked to the
			 * context ISubject. If we expect to need to access previous
			 * history, we must ask for the number of back steps we want to be
			 * allowed to make. At the moment the system automatically keeps
			 * full history for outputs, and assumes that inputs don't need to.
			 * Later we will be able to use a parameter to getState() to
			 * indicate the lenght of the history we need.
			 */
			IState outState = context.getState(obs);

			/**
			 * To access all the states within a transition (e.g. all points in
			 * space at the time) we use the scale index. and pass the
			 * transition, which implements IScale.Locator - an interface that
			 * "locks" one or more dimensions and returns an iterator for the
			 * states along the others. We pass a null here, which is understood
			 * as the initialization transition. There are locators for space.
			 */
			Random random = new Random();
			for (int n : scale.getIndex((IScale.Locator) null)) {

				/*
				 * we set the output values to a scrambled version of the first
				 * input if the model has inputs, or to a number between 0 and
				 * 500 if not.
				 */
				double value = 0;
				if (inputState != null) {

					/**
					 * use States.getDouble() to keep state access simple. It
					 * can be also passed a transition so that the "current"
					 * value is retrieved; this one retrieves the initial value.
					 */
					double m = random.nextDouble() * 100.0 - 50.0;
					value = States.getDouble(inputState, n);
					if (!Double.isNaN(value)) {
						value += m;
						if (value < 0) {
							value = 0;
						}
					}
				} else {
					value = random.nextDouble() * 500.0;
				}

				/**
				 * Set the value in the state using methods in the utility class
				 * States.
				 */
				States.set(outState, value, n);
			}

			/**
			 * Set the state as an output. This phase isn't strictly necessary
			 * as createState() has already created it in the subject - API may
			 * change later.
			 */
			outputStates.put(out, outState);

		}

		/**
		 * the software will take care of setting these inputs in the context or
		 * streaming them back to the calling engine if we're a remote service.
		 */
		return outputStates;
	}

	/**
	 * This is called at each temporal transition and should compute the
	 * relative outputs based on the current states of all inputs. ITransition
	 * extends IScale and contains the same extents as the process' scale,
	 * except its time multiplicity will be 1 and the time extent will only
	 * represent the time we're handling in it (one timestep, but it could be
	 * any implementation of time, regular or not).
	 */
	@Override
	public Map<String, IObservation> compute(ITransition transition, Map<String, IState> inputs) throws KlabException {

		Map<String, IObservation> ret = new HashMap<>();

		/**
		 * check the transition: if it's the last, no need to keep this object
		 * around.
		 */
		canDispose = transition.isLast();

		/**
		 * We will use the value at the previous transition to modify it. The
		 * index we take from the scale.getIndex() iterator is the full offset
		 * that considers all extents, so we will need to pass the transition to
		 * access the value. Because transition.previous() builds a new
		 * transition, we do it once outside the loop.
		 */
		final ITransition previous = transition.previous();
		Random random = new Random();

		/**
		 * Simply perturb the output states. Use the iterator for the spatial
		 * dimension in the time slice identified by the transition. This will
		 * return the actual offsets in the full scale, which we can pass to
		 * States.set() and get() to ensure proper addressing. Previous states
		 * (including the one we're modifying) are accessed by passing the
		 * previous transition.
		 */
		for (int n : scale.getIndex(transition)) {

			/**
			 * If a specific extent offset is needed, e.g. the offset in space,
			 * the following can be used, which will return the spatial offset
			 * for the given overall offset. The reason for not having specific
			 * getSpatialOffset() or getTemporalOffset is that there can be
			 * additional extents beyond time and space, although these are not
			 * used at the moment.
			 */
			int spaceOffset = scale.getExtentOffset(scale.getSpace(), n);
			/**
			 * The spatial offset can be converted to an IExtent like so: (this
			 * works also at initialize() of course)
			 */
			ISpatialExtent currentSpace = scale.getSpace().getExtent(spaceOffset);

			/*
			 * if you need a point in space in lat/lon, use the centroid of the
			 * standardized geometry (guaranteed to have x = lon and y = lat);
			 */
			Point point = ((IGeometricShape) currentSpace).getStandardizedGeometry().getCentroid();

			/**
			 * The Extent will be the 1-dimensional portion of the whole
			 * topology, so a grid cell in a grid, or a polygon in a set of
			 * polygons, a period in a time grid etc.
			 * 
			 * However, the above may be inefficient: for example, currently in
			 * gridded space, the above will build a polygon (the only generic
			 * spatial extent) for each cell. I will eventually have a GridCell
			 * type that will allow the above with more efficiency. But if we
			 * assume that the model will usually be run on gridded space, we
			 * can write the very fast code below:
			 */
			if (scale.getSpace().getGrid() != null) {
				int[] xy = scale.getSpace().getGrid().getXYOffsets(spaceOffset);

				// uncommenting this will increase run time enormously!
				// Thinklab.logger().info("running on grid cell (" + xy[0] + ","
				// + xy[1] + ")");
			}

			/**
			 * This will give us the x,y coordinates (Thinklab forces x to be
			 * the horizontal axis irrespective of spatial projection). Lat/lon
			 * or other info can be asked to the grid - if something's missing
			 * just ask.
			 * 
			 * We can constrain a model to run on a grid at the semantic side by
			 * adding a "over space( grid = unknown)" statement. Note, however,
			 * that this won't translate into an error before the model is run
			 * (won't be visible in the GUI) so it's best to check our
			 * assumptions by throwing an exception at initialize() if the scale
			 * is not what we expect.
			 */

			/*
			 * we set the output values to a scrambled version of the first
			 * input if the model has inputs, or to a number between 0 and 500
			 * if not.
			 */
			double value = 0;

			for (IObservation o : outputStates.values()) {

				/**
				 * use States.getDouble() to keep state access simple. We must
				 * pass the transition when it's not initializing, or we get a
				 * nasty access error.
				 */
				double m = random.nextDouble() * 100.0 - 50.0;

				/**
				 * This gets the value correspondent to the offset n at the
				 * passed transition. What we're asking for here is the
				 * "current" value before the new one is computed and set below.
				 */
				value = States.getDouble((IState) o, n, previous);
				if (!Double.isNaN(value)) {
					value += m;
					if (value < 0) {
						value = 0;
					}
				}

				/**
				 * To set the current state value, use States.set.
				 */
				States.set((IState) o, value, n);
			}

		}

		ret.putAll(outputStates);

		return ret;
	}

	@Override
	public void setContext(Map<String, Object> parameters, IModel model, IProject project) {

		if (parameters.containsKey("multiplier")) {
			multiplier = ((Number) parameters.get("multiplier")).intValue();
		}
	}

}
