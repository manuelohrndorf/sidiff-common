package org.sidiff.common.ui.widgets;

import java.util.Objects;
import java.util.function.Function;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * <p>An abstract widget which uses an {@link IContainerFactory} to create the main
 * container to contain the widget's controls. The main container can have a title.
 * {@link DefaultContainerFactory} contains default container factory implementations.</p>
 * <p>Instead of overriding {@link #createControl(Composite)}, the method {@link #createContents(Composite)}
 * must be overridden instead when implementing this class.</p>
 * @author Robert Müller
 */
public abstract class AbstractContainerWidget extends AbstractWidget {

	private String title;
	private Composite composite;
	private IContainerFactory containerFactory;

	/**
	 * Equivalent to AbstractContainerWidget({@link DefaultContainerFactory#GROUP})
	 */
	public AbstractContainerWidget() {
		this(DefaultContainerFactory.GROUP);
	}

	/**
	 * Creates the container widget using the given factory.
	 * @param containerFactory the container factory
	 */
	public AbstractContainerWidget(IContainerFactory containerFactory) {
		setContainerFactory(containerFactory);
	}

	/**
	 * Finally overridden in {@link AbstractContainerWidget}.
	 * Implement {@link #createContents(Composite)} instead to create the widget contents.
	 */
	@Override
	public final Composite createControl(Composite parent) {
		Assert.isNotNull(getTitle(), "Title of AbstractContainerWidget must be set before its controls are created");
		composite = containerFactory.createContainer(parent, getTitle(), getWidgetCallback(), this::createContents);
		return composite;
	}

	/**
	 * Finally overridden in {@link AbstractContainerWidget}.
	 * Implement {@link #createContents(Composite)} instead to create the widget contents.
	 */
	@Override
	public final Composite getWidget() {
		return composite;
	}

	/**
	 * Creates the widget's contents.
	 * @param container the container for the widget's contents
	 * @return the composite containing the widgets contents
	 */
	protected abstract Composite createContents(Composite container);

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public IContainerFactory getContainerFactory() {
		return containerFactory;
	}

	public void setContainerFactory(IContainerFactory containerFactory) {
		this.containerFactory = containerFactory;
	}


	/**
	 * A factory that creates container composites.
	 */
	@FunctionalInterface
	public interface IContainerFactory {
		Composite createContainer(Composite parent, String title,
				IWidgetCallback.Callback callback, Function<Composite,Composite> childrenFactory);
	}

	/**
	 * Default implementations for {@link IContainerFactory}.
	 */
	public enum DefaultContainerFactory implements IContainerFactory {
		/**
		 * Does not create a container, only creates the children.
		 * Does not support title.
		 */
		FLAT((parent, title, callback, childrenFactory) -> childrenFactory.apply(parent)),

		/**
		 * Creates a {@link Group} with the title.
		 */
		GROUP((parent, title, callback, childrenFactory) -> {
			Group group = new Group(parent, SWT.NONE);
			GridLayoutFactory.fillDefaults().margins(2, 2).applyTo(group);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
			group.setText(title);
			childrenFactory.apply(group);
			return group;
		}),

		/**
		 * Creates an {@link ExpandableComposite} with the title and a border.
		 */
		EXPANDABLE((parent, title, callback, childrenFactory) -> {
			ExpandableComposite expandable = new ExpandableComposite(parent, SWT.BORDER);
			GridLayoutFactory.fillDefaults().margins(2, 2).applyTo(expandable);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(expandable);
			expandable.setText(title);
			Composite child = childrenFactory.apply(expandable);
			if(child == expandable) {
				throw new RuntimeException(
						"AbstractContainerWidget.createContents must create a separate "
						+ "composite parent when using DefaultContainerFactory.EXPANDABLE");
			}
			expandable.setClient(child);
			expandable.addExpansionListener(new IExpansionListener() {
				@Override
				public void expansionStateChanging(ExpansionEvent e) {
					//
				}
				@Override
				public void expansionStateChanged(ExpansionEvent e) {
					callback.requestLayout();
				}
			});
			return expandable;
		});


		private final IContainerFactory delegate;

		private DefaultContainerFactory(IContainerFactory delegate) {
			this.delegate = Objects.requireNonNull(delegate);
		}

		@Override
		public Composite createContainer(Composite parent, String title,
				IWidgetCallback.Callback callback, Function<Composite, Composite> childrenFactory) {
			return delegate.createContainer(parent, title, callback, childrenFactory);
		}
	}
}
