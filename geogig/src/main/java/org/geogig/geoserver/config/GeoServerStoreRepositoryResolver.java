package org.geogig.geoserver.config;

import java.io.IOException;
import java.net.URI;

import org.locationtech.geogig.geotools.data.GeoGigDataStoreFactory;

import com.google.common.base.Throwables;

public class GeoServerStoreRepositoryResolver implements GeoGigDataStoreFactory.RepositoryLookup {

    @Override
    public URI resolve(final String repository) {
        RepositoryManager repositoryManager = RepositoryManager.get();
        try {
            RepositoryInfo info = repositoryManager.get(repository);
            return URI.create(info.getLocation());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
