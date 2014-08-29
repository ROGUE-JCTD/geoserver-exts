package org.geogig.geoserver.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.geoserver.catalog.Predicates.and;
import static org.geoserver.catalog.Predicates.equal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.util.CloseableIterator;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.web.GeoServerApplication;
import org.locationtech.geogig.api.ContextBuilder;
import org.locationtech.geogig.api.GeoGIG;
import org.locationtech.geogig.api.GlobalContextBuilder;
import org.locationtech.geogig.api.porcelain.InitOp;
import org.locationtech.geogig.cli.CLIContextBuilder;
import org.locationtech.geogig.geotools.data.GeoGigDataStoreFactory;
import org.locationtech.geogig.repository.Repository;
import org.opengis.filter.Filter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class RepositoryManager {
    static {
        if (GlobalContextBuilder.builder == null
                || GlobalContextBuilder.builder.getClass().equals(ContextBuilder.class)) {
            GlobalContextBuilder.builder = new CLIContextBuilder();
        }
    }

    private ConfigStore store;

    public static RepositoryManager get() {
        RepositoryManager repoManager = GeoServerExtensions.bean(RepositoryManager.class);
        Preconditions.checkState(repoManager != null);
        return repoManager;
    }

    public RepositoryManager(ConfigStore store) {
        checkNotNull(store);
        this.store = store;
    }

    public List<RepositoryInfo> getAll() {
        return store.getRepositories();
    }

    public RepositoryInfo get(final String repoId) throws IOException {
        return store.load(repoId);
    }

    public List<DataStoreInfo> findGeogigStores() {
        return findGeogigStores(catalog());
    }

    private Catalog catalog() {
        return GeoServerApplication.get().getCatalog();
    }

    static List<DataStoreInfo> findGeogigStores(Catalog catalog) {
        List<DataStoreInfo> geogigStores;
        org.opengis.filter.Filter filter = equal("type", GeoGigDataStoreFactory.DISPLAY_NAME);
        CloseableIterator<DataStoreInfo> stores = catalog.list(DataStoreInfo.class, filter);
        try {
            geogigStores = Lists.newArrayList(stores);
        } finally {
            stores.close();
        }

        return geogigStores;
    }

    public List<DataStoreInfo> findDataStoes(final String repoLocation) {
        Filter filter = equal("type", GeoGigDataStoreFactory.DISPLAY_NAME);

        String locationKey = "connectionParameters." + GeoGigDataStoreFactory.REPOSITORY.key;
        filter = and(filter, equal(locationKey, repoLocation));
        List<DataStoreInfo> dependent;
        try (CloseableIterator<DataStoreInfo> stores = catalog().list(DataStoreInfo.class, filter)) {
            dependent = Lists.newArrayList(stores);
        }
        return dependent;
    }

    public List<? extends CatalogInfo> findDependentCatalogObjects(final String repoLocation) {
        Filter filter = equal("type", GeoGigDataStoreFactory.DISPLAY_NAME);

        String locationKey = "connectionParameters." + GeoGigDataStoreFactory.REPOSITORY.key;
        filter = and(filter, equal(locationKey, repoLocation));
        List<DataStoreInfo> stores = findDataStoes(repoLocation);
        List<CatalogInfo> dependent = new ArrayList<CatalogInfo>(stores);
        Catalog catalog = catalog();
        for (DataStoreInfo store : stores) {
            List<FeatureTypeInfo> ftypes = catalog.getFeatureTypesByDataStore(store);
            dependent.addAll(ftypes);
            for (FeatureTypeInfo ftype : ftypes) {
                dependent.addAll(catalog.getLayers(ftype));
            }
        }

        return dependent;
    }

    public List<LayerInfo> findLayers(DataStoreInfo store) {
        Filter filter = equal("resource.store.id", store.getId());
        try (CloseableIterator<LayerInfo> it = catalog().list(LayerInfo.class, filter)) {
            return Lists.newArrayList(it);
        }
    }

    public List<FeatureTypeInfo> findFeatureTypes(DataStoreInfo store) {
        Filter filter = equal("store.id", store.getId());
        try (CloseableIterator<FeatureTypeInfo> it = catalog().list(FeatureTypeInfo.class, filter)) {
            return Lists.newArrayList(it);
        }
    }

    public static boolean isGeogigDirectory(final File file) {
        if (file == null) {
            return false;
        }
        final File geogigDir = new File(file, ".geogig");
        final boolean isGeogigDirectory = geogigDir.exists() && geogigDir.isDirectory();
        return isGeogigDirectory;
    }

    public RepositoryInfo save(RepositoryInfo info) {
        Preconditions.checkNotNull(info.getName());
        Preconditions.checkNotNull(info.getParentDirectory());
        if (info.getId() == null && !isGeogigDirectory(new File(info.getLocation()))) {
            create(info);
        }
        return store.save(info);
    }

    private void create(final RepositoryInfo repoInfo) {
        File targetDirectory = new File(repoInfo.getLocation());
        Preconditions.checkArgument(!isGeogigDirectory(targetDirectory));

        File parentDirectory = new File(repoInfo.getParentDirectory());
        GeoGIG geogig = new GeoGIG(parentDirectory);
        try {
            Repository repository = geogig.command(InitOp.class).setTarget(targetDirectory).call();
            Preconditions.checkState(repository != null);
        } finally {
            geogig.close();
        }
    }
}
