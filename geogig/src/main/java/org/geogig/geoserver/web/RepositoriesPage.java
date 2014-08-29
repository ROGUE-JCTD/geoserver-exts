/* Copyright (c) 2001 - 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geogig.geoserver.web;

import java.io.File;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.geogig.geoserver.config.RepositoryInfo;
import org.geogig.geoserver.web.repository.DirectoryChooser;
import org.geogig.geoserver.web.repository.RepositoriesListPanel;
import org.geoserver.web.GeoServerSecuredPage;

/**
 * Add/edit/remove repositories
 */
public class RepositoriesPage extends GeoServerSecuredPage {

    private final ModalWindow repoChooserWindow;

    private final RepositoriesListPanel table;

    private File lastSelectedDirectory;

    public RepositoriesPage() {

        table = new RepositoriesListPanel("table");
        table.setOutputMarkupId(true);
        add(table);

        // add the dialog for the repository chooser
        add(repoChooserWindow = new ModalWindow("modalWindow"));

        setHeaderPanel(headerPanel());
    }

    protected Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);

        IModel<RepositoryInfo> newInfo = new Model<RepositoryInfo>(new RepositoryInfo());
        Form<RepositoryInfo> chooseRepoForm = new Form<RepositoryInfo>("chooseRepoForm", newInfo);
        chooseRepoForm.add(chooserButton(chooseRepoForm));
        header.add(chooseRepoForm);

        header.add(new BookmarkablePageLink<String>("addNew", RepositoryEditPage.class));

        return header;
    }

    protected Component chooserButton(Form<RepositoryInfo> form) {
        AjaxSubmitLink link = new AjaxSubmitLink("importExisting", form) {

            private static final long serialVersionUID = 1242472443848716943L;

            @Override
            public boolean getDefaultFormProcessing() {
                return false;
            }

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                IModel<File> model = new Model<File>(lastSelectedDirectory);
                DirectoryChooser chooser;
                chooser = new DirectoryChooser(repoChooserWindow.getContentId(), model) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void geogigDirectoryClicked(final File file, AjaxRequestTarget target) {
                        // clear the raw input of the field won't show the new model value
                        System.err.println("Stub method to add an existing repository");
                        repoChooserWindow.close(target);
                    };

                    @Override
                    protected void directoryClicked(File file, AjaxRequestTarget target) {
                        lastSelectedDirectory = file;
                        super.directoryClicked(file, target);
                    }
                };
                chooser.setFileTableHeight(null);
                repoChooserWindow.setContent(chooser);
                repoChooserWindow.setTitle(new ResourceModel("GeoGigDirectory.chooser.browseTitle"));
                repoChooserWindow.show(target);
            }

        };
        return link;
    }
}
