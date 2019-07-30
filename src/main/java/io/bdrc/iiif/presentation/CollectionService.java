package io.bdrc.iiif.presentation;

import static io.bdrc.iiif.presentation.AppConstants.BDR_len;
import static io.bdrc.iiif.presentation.AppConstants.GENERIC_APP_ERROR_CODE;
import static io.bdrc.iiif.presentation.AppConstants.IIIFPresPrefix;
import static io.bdrc.iiif.presentation.AppConstants.IIIFPresPrefix_coll;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.digitalcollections.iiif.model.PropertyValue;
import de.digitalcollections.iiif.model.enums.ViewingHint;
import de.digitalcollections.iiif.model.sharedcanvas.Collection;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import io.bdrc.iiif.presentation.exceptions.BDRCAPIException;
import io.bdrc.iiif.presentation.models.Identifier;
import io.bdrc.iiif.presentation.models.ItemInfo;
import io.bdrc.iiif.presentation.models.ItemInfo.VolumeInfoSmall;
import io.bdrc.iiif.presentation.models.LangString;
import io.bdrc.iiif.presentation.models.Location;
import io.bdrc.iiif.presentation.models.PartInfo;
import io.bdrc.iiif.presentation.models.WorkInfo;

public class CollectionService {

    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);
    // public static final List<ViewingHint> VIEW_HINTS=Arrays.asList(new
    // ViewingHint[] { ViewingHint.MULTI_PART});
    public static final String VIEWING_HINTS = "multi-part";

    public static String getPrefixedForm(final String id) {
        return "bdr:" + id.substring(BDR_len);
    }

    public static Collection getCollectionForIdentifier(final Identifier id, boolean continuous) throws BDRCAPIException {
        switch (id.getSubType()) {
        case Identifier.COLLECTION_ID_ITEM:
        case Identifier.COLLECTION_ID_ITEM_VOLUME_OUTLINE:
        case Identifier.COLLECTION_ID_WORK_IN_ITEM:
            return getCollectionForItem(getCommonCollection(id), id, continuous);
        case Identifier.COLLECTION_ID_WORK_OUTLINE:
            return getCollectionForOutline(getCommonCollection(id), id, continuous);
        default:
            throw new BDRCAPIException(404, GENERIC_APP_ERROR_CODE, "you cannot access this type of manifest yet");
        }
    }

    public static void addManifestsForLocation(final Collection c, final WorkInfo wi, final ItemInfo ii, final boolean continuous) {
        if (!wi.hasLocation)
            return;
        final Location loc = wi.location;
        final boolean needsVolumeIndication = loc.evolnum - loc.bvolnum > 2;
        for (int i = loc.bvolnum; i <= loc.evolnum; i++) {
            VolumeInfoSmall vi = ii.getVolumeNumber(i);
            if (vi == null)
                continue;
            final int volumebPage = (i == loc.bvolnum) ? loc.bpagenum : 0;
            final int volumeePage = (i == loc.evolnum) ? loc.epagenum : -1;
            final StringBuilder sb = new StringBuilder();
            sb.append(IIIFPresPrefix + "v:" + vi.getPrefixedUri());
            if (volumebPage != 0 || volumeePage != -1) {
                sb.append("::");
                if (volumebPage != 0)
                    sb.append(volumebPage);
                sb.append("-");
                if (volumeePage != -1)
                    sb.append(volumeePage);
            }
            sb.append("/manifest");
            if (continuous) {
                sb.append("?continuous=true");
            }
            final Manifest m = new Manifest(sb.toString());
            m.setLabel(ManifestService.getLabel(i, wi, needsVolumeIndication));
            c.addManifest(m);
        }
    }

    public static PropertyValue getLabels(String workId, WorkInfo wi) {
        final PropertyValue label = new PropertyValue();
        if (wi.labels == null || wi.labels.isEmpty()) {
            label.addValue(workId);
            return label;
        }
        for (LangString ls : wi.labels) {
            if (ls.language != null)
                label.addValue(ManifestService.getLocaleFor(ls.language), ls.value);
            else
                label.addValue(ls.value);
        }
        return label;
    }

    public static Collection getCommonCollection(final Identifier id) {
        final Collection collection = new Collection(IIIFPresPrefix_coll + id.getId(), "Collection");
        collection.setAttribution(ManifestService.attribution);
        ArrayList<ViewingHint> vh = new ArrayList<>();
        vh.add(ViewingHint.MULTI_PART);
        collection.setViewingHints(vh);
        // TODO: use the actual license
        collection.addLicense("https://creativecommons.org/publicdomain/mark/1.0/");
        collection.addLogo("https://s3.amazonaws.com/bdrcwebassets/prod/iiif-logo.png");
        return collection;
    }

    public static Collection getCollectionForOutline(final Collection collection, final Identifier id, final boolean continuous) throws BDRCAPIException {
        final WorkInfo wi = WorkInfoService.getWorkInfo(id.getWorkId());
        final ItemInfo ii;
        logger.info("building outline collection for ID {}", id.getId());
        collection.setLabel(getLabels(id.getWorkId(), wi));
        if (wi.parts != null) {
            for (final PartInfo pi : wi.parts) {
                final String collectionId = "wio:" + pi.partId;
                final Collection subcollection = new Collection(IIIFPresPrefix_coll + collectionId);
                final PropertyValue labels = ManifestService.getPropForLabels(pi.labels);
                subcollection.setLabel(labels);
                collection.addCollection(subcollection);
            }
        }
        if (id.getItemId() != null) {
            ii = ItemInfoService.getItemInfo(id.getItemId());
        } else if (wi.itemId != null) {
            ii = ItemInfoService.getItemInfo(wi.itemId);
        } else {
            // TODO: exception of wi.parts == null ? currently an empty collection is
            // returned
            return collection;
        }
        if (wi.hasLocation) {
            addManifestsForLocation(collection, wi, ii, continuous);
        } else if (wi.isRoot) {
            final String volPrefix = "v:";
            boolean needsVolumeIndication = ii.volumes.size() > 1;
            for (ItemInfo.VolumeInfoSmall vi : ii.volumes) {
                final String manifestId = volPrefix + vi.getPrefixedUri();
                String manifestUrl;
                if (vi.iiifManifest != null) {
                    manifestUrl = vi.iiifManifest;
                } else {
                    manifestUrl = IIIFPresPrefix + manifestId + "/manifest";
                    if (continuous) {
                        manifestUrl += "?continuous=true";
                    }
                }
                final Manifest manifest = new Manifest(manifestUrl);
                manifest.setLabel(ManifestService.getLabel(vi.volumeNumber, wi, needsVolumeIndication));
                collection.addManifest(manifest);
            }
        }
        return collection;
    }

    public static Collection getCollectionForItem(final Collection collection, final Identifier id, final boolean continuous) throws BDRCAPIException {
        final ItemInfo ii;
        if (id.getItemId() == null) {
            final WorkInfo wi = WorkInfoService.getWorkInfo(id.getWorkId());
            ii = ItemInfoService.getItemInfo(wi.itemId);
        } else {
            ii = ItemInfoService.getItemInfo(id.getItemId());
        }
        logger.info("building item collection for ID {}", id.getId());
        collection.addLabel(id.getItemId());
        final String volPrefix = id.getSubType() == Identifier.COLLECTION_ID_ITEM_VOLUME_OUTLINE ? "vo:" : "v:";
        for (ItemInfo.VolumeInfoSmall vi : ii.volumes) {
            final String manifestId = volPrefix + vi.getPrefixedUri();
            String manifestUrl;
            if (vi.iiifManifest != null) {
                manifestUrl = vi.iiifManifest;
            } else {
                manifestUrl = IIIFPresPrefix + manifestId + "/manifest";
                if (continuous) {
                    manifestUrl += "?continuous=true";
                }
            }
            final Manifest manifest = new Manifest(manifestUrl);
            manifest.setLabel(vi.getLabel());
            collection.addManifest(manifest);
        }
        return collection;
    }
}
