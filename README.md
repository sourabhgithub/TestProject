private static Map<String, List<String>> getMetaDataForUploadedFile() {
    Map<String, List<String>> allowedFileTypes = new HashMap<>();

    // Documents
    allowedFileTypes.put("text/plain", List.of(".txt"));
    allowedFileTypes.put("application/rtf", List.of(".rtf"));
    allowedFileTypes.put("application/vnd.oasis.opendocument.text", List.of(".odt"));
    allowedFileTypes.put("application/pdf", List.of(".pdf"));
    allowedFileTypes.put("application/msword", List.of(".doc"));
    allowedFileTypes.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", List.of(".docx"));
    allowedFileTypes.put("application/vnd.ms-excel", List.of(".xls"));
    allowedFileTypes.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", List.of(".xlsx"));
    allowedFileTypes.put("application/vnd.oasis.opendocument.spreadsheet", List.of(".ods"));
    allowedFileTypes.put("application/vnd.ms-powerpoint", List.of(".ppt"));
    allowedFileTypes.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", List.of(".pptx"));
    
    // Images
    allowedFileTypes.put("image/jpeg", List.of(".jpeg"));
    allowedFileTypes.put("image/jpg", List.of(".jpg"));
    allowedFileTypes.put("image/png", List.of(".png"));
    allowedFileTypes.put("image/gif", List.of(".gif"));
    allowedFileTypes.put("image/bmp", List.of(".bmp"));
    allowedFileTypes.put("image/webp", List.of(".webp"));

    // Add application/octet-stream for all extensions
    List<String> allExtensions = List.of(
        ".txt", ".rtf", ".odt", ".pdf", ".doc", ".docx", ".xls", ".xlsx", 
        ".ods", ".ppt", ".pptx", ".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"
    );
    allowedFileTypes.put("application/octet-stream", allExtensions);

    return allowedFileTypes;
}
