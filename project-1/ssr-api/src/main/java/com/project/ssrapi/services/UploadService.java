package com.project.ssrapi.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.shinsiri.configs.AWSS3Properties;
import com.example.shinsiri.dtos.req.FileMetadata;
import com.example.shinsiri.dtos.req.upload.*;
import com.example.shinsiri.entities.*;
import com.example.shinsiri.exceptions.BadRequestException;
import com.example.shinsiri.repositories.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
    private static final String CUSTOMER_FOLDER = "customers";
    private static final String USER_FOLDER = "users";
    private static final String PRODUCT_FOLDER = "prodcuts";
    private static final String ORDER_FOLDER = "orders";
    private static final String FILE_FOLDER = "files";
    private static final int MAX_IMAGE_SIZE = (2 * 1024); // 2MB
    private static final int MAX_FILE_SIZE = (20 * 1024); // 20MB

    @Autowired
    private CustomerImageRepository customerImageRepository;
    @Autowired
    private UserImageRepository userImageRepository;
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private OrderImageRepository orderImageRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private AWSS3Properties awss3Properties;

    public FileMetadata uploadCustomerImage(ReqUploadCustomerImageDTO dto) {
        InputStream bis = null;
        try {
            //[1] GEN IMAGE NAME
            String genImageName = dto.getImageName() + "_" + UUID.randomUUID();
            String genImageNameWithPath = CUSTOMER_FOLDER + File.separator + genImageName;
            byte[] imageByteArray = handleBase64ToByte(dto.getImageData());
            if (checkSizeIsToLargeFromByte(MAX_IMAGE_SIZE, imageByteArray))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Maximum image size is [%s MB]", (MAX_IMAGE_SIZE / 1024)));

            //[2] UPLOAD S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageByteArray.length);
            metadata.setContentType("image/jpeg");
            metadata.setCacheControl("max-age=3600");

            bis = new ByteArrayInputStream(imageByteArray);
            amazonS3.putObject(awss3Properties.getBucket(), genImageNameWithPath, bis, metadata);
            String imageUrl = amazonS3.getUrl(awss3Properties.getBucket(), genImageNameWithPath).toString();

            saveCustomerImage(dto.getCustomerId(), dto.getType(), imageUrl, dto.getImageName());

            //[3] RESPONSE
            logger.info("\n[UploadImage] Upload Image Successfully:\n# IMAGE_NAME: {}\n# IMAGE_URL: {}\n", genImageName, imageUrl);
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setPath(imageUrl);
            fileMetadata.setFileName(genImageName);
            fileMetadata.setFileBase64(handleBase64(dto.getImageData()));
            return fileMetadata;

        } catch (Exception ex) {
            logger.error("[Exception][UploadImage] : ", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, Can't upload image", ex);

        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    private void saveCustomerImage(int customerId, String type, String imagePath, String imageName) {
        CustomerImage oldImage = customerImageRepository.findOneByCustomerIdAndType(customerId, type);
        if (oldImage != null) {
            customerImageRepository.delete(oldImage);
        }

        CustomerImage customerImage = new CustomerImage();
        customerImage.setCustomerId(customerId);
        customerImage.setType(type);
        customerImage.setImagePath(imagePath);
        customerImage.setImageName(imageName);

        customerImageRepository.saveAndFlush(customerImage);
    }

    public FileMetadata uploadUserImage(ReqUploadUserImageDTO dto) {
        InputStream bis = null;
        try {
            //[1] GEN IMAGE NAME
            String genImageName = dto.getImageName() + "_" + UUID.randomUUID();
            String genImageNameWithPath = USER_FOLDER + File.separator + genImageName;
            byte[] imageByteArray = handleBase64ToByte(dto.getImageData());
            if (checkSizeIsToLargeFromByte(MAX_IMAGE_SIZE, imageByteArray))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Maximum image size is [%s MB]", (MAX_IMAGE_SIZE / 1024)));

            //[2] UPLOAD S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageByteArray.length);
            metadata.setContentType("image/jpeg");
            metadata.setCacheControl("max-age=3600");

            bis = new ByteArrayInputStream(imageByteArray);
            amazonS3.putObject(awss3Properties.getBucket(), genImageNameWithPath, bis, metadata);
            String imageUrl = amazonS3.getUrl(awss3Properties.getBucket(), genImageNameWithPath).toString();

            saveUserImage(dto.getUserId(), dto.getType(), imageUrl, dto.getImageName());

            //[3] RESPONSE
            logger.info("\n[UploadImage] Upload Image Successfully:\n# IMAGE_NAME: {}\n# IMAGE_URL: {}\n", genImageName, imageUrl);
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setPath(imageUrl);
            fileMetadata.setFileName(genImageName);
            fileMetadata.setFileBase64(handleBase64(dto.getImageData()));
            return fileMetadata;

        } catch (Exception ex) {
            logger.error("[Exception][UploadImage] : ", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, Can't upload image", ex);

        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    private void saveUserImage(int userId, String type, String imagePath, String imageName) {
        UserImage oldImage = userImageRepository.findOneByUserIdAndType(userId, type);
        if (oldImage != null) {
            userImageRepository.delete(oldImage);
        }

        UserImage userImage = new UserImage();
        userImage.setUserId(userId);
        userImage.setType(type);
        userImage.setImagePath(imagePath);
        userImage.setImageName(imageName);

        userImageRepository.saveAndFlush(userImage);
    }

    public FileMetadata uploadProductImage(ReqUploadProductImageDTO dto) {
        InputStream bis = null;
        try {
            //[1] GEN IMAGE NAME
            String genImageName = dto.getImageName() + "_" + UUID.randomUUID();
            String genImageNameWithPath = PRODUCT_FOLDER + File.separator + genImageName;
            byte[] imageByteArray = handleBase64ToByte(dto.getImageData());
            if (checkSizeIsToLargeFromByte(MAX_IMAGE_SIZE, imageByteArray))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Maximum image size is [%s MB]", (MAX_IMAGE_SIZE / 1024)));

            //[2] UPLOAD S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageByteArray.length);
            metadata.setContentType("image/jpeg");
            metadata.setCacheControl("max-age=3600");

            bis = new ByteArrayInputStream(imageByteArray);
            amazonS3.putObject(awss3Properties.getBucket(), genImageNameWithPath, bis, metadata);
            String imageUrl = amazonS3.getUrl(awss3Properties.getBucket(), genImageNameWithPath).toString();

            saveProductImage(dto.getProductId(), dto.getType(), imageUrl, dto.getImageName());

            //[3] RESPONSE
            logger.info("\n[UploadImage] Upload Image Successfully:\n# IMAGE_NAME: {}\n# IMAGE_URL: {}\n", genImageName, imageUrl);
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setPath(imageUrl);
            fileMetadata.setFileName(genImageName);
            fileMetadata.setFileBase64(handleBase64(dto.getImageData()));
            return fileMetadata;

        } catch (Exception ex) {
            logger.error("[Exception][UploadImage] : ", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, Can't upload image", ex);

        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    private void saveProductImage(int productId, String type, String imagePath, String imageName) {
        ProductImage oldImage = productImageRepository.findOneByProductIdAndType(productId, type);
        if (oldImage != null) {
            productImageRepository.delete(oldImage);
        }

        ProductImage productImage = new ProductImage();
        productImage.setProductId(productId);
        productImage.setType(type);
        productImage.setImagePath(imagePath);
        productImage.setImageName(imageName);

        productImageRepository.saveAndFlush(productImage);
    }

    public FileMetadata uploadOrderImage(ReqUploadOrderImageDTO dto) {
        InputStream bis = null;
        try {
            //[1] GEN IMAGE NAME
            String genImageName = dto.getImageName() + "_" + UUID.randomUUID();
            String genImageNameWithPath = ORDER_FOLDER + File.separator + genImageName;
            byte[] imageByteArray = handleBase64ToByte(dto.getImageData());
            if (checkSizeIsToLargeFromByte(MAX_IMAGE_SIZE, imageByteArray))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Maximum image size is [%s MB]", (MAX_IMAGE_SIZE / 1024)));

            //[2] UPLOAD S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageByteArray.length);
            metadata.setContentType("image/jpeg");
            metadata.setCacheControl("max-age=3600");

            bis = new ByteArrayInputStream(imageByteArray);
            amazonS3.putObject(awss3Properties.getBucket(), genImageNameWithPath, bis, metadata);
            String imageUrl = amazonS3.getUrl(awss3Properties.getBucket(), genImageNameWithPath).toString();

            saveOrderImage(dto.getOrderId(), dto.getType(), imageUrl, dto.getImageName());

            //[3] RESPONSE
            logger.info("\n[UploadImage] Upload Image Successfully:\n# IMAGE_NAME: {}\n# IMAGE_URL: {}\n", genImageName, imageUrl);
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setPath(imageUrl);
            fileMetadata.setFileName(genImageName);
            fileMetadata.setFileBase64(handleBase64(dto.getImageData()));
            return fileMetadata;

        } catch (Exception ex) {
            logger.error("[Exception][UploadImage] : ", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, Can't upload image", ex);

        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    private void saveOrderImage(int orderId, String type, String imagePath, String imageName) {
        OrderImage oldImage = orderImageRepository.findOneByOrderIdAndType(orderId, type);
        if (oldImage != null) {
            orderImageRepository.delete(oldImage);
        }

        OrderImage orderImage = new OrderImage();
        orderImage.setOrderId(orderId);
        orderImage.setType(type);
        orderImage.setImagePath(imagePath);
        orderImage.setImageName(imageName);

        orderImageRepository.saveAndFlush(orderImage);

        updateStatusWhenUploadLicense(orderId, type);
    }

    private void updateStatusWhenUploadLicense(int orderId, String type) {
        if (type.equals("LICENSE_1") || type.equals("LICENSE_2")) {
            Order order = orderRepository.findOneById(orderId);
            order.setStatus(type.equals("LICENSE_1") ? OrderService.WAIT_LICENSE_2_STATUS : OrderService.WAIT_APPROVE_STATUS);

            orderRepository.saveAndFlush(order);
        }
    }

    public FileMetadata uploadFile(ReqUploadFileDTO dto) {
        InputStream bis = null;
        try {
            //[1] GEN FILE NAME
            String genFileName = dto.getFileName() + "_" + UUID.randomUUID();
            String genFileNameWithPath = FILE_FOLDER + File.separator + genFileName;
            byte[] fileByteArray = handleBase64ToByte(dto.getFileData());
            if (checkSizeIsToLargeFromByte(MAX_FILE_SIZE, fileByteArray))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Maximum file size is [%s MB]", (MAX_FILE_SIZE / 1024)));

            //[2] UPLOAD S3
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileByteArray.length);
            metadata.setContentType(getContentTypeFromType(dto.getType()));
            metadata.setCacheControl("max-age=3600");

            bis = new ByteArrayInputStream(fileByteArray);
            amazonS3.putObject(awss3Properties.getBucket(), genFileNameWithPath, bis, metadata);
            String imageUrl = amazonS3.getUrl(awss3Properties.getBucket(), genFileNameWithPath).toString();

            saveFile(dto.getFileId(), imageUrl, dto.getFileName());

            //[3] RESPONSE
            logger.info("\n[UploadFile] Upload File Successfully:\n# FILE_NAME: {}\n# FILE_URL: {}\n", genFileName, imageUrl);
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setPath(imageUrl);
            fileMetadata.setFileName(genFileName);
            fileMetadata.setFileBase64(handleBase64(dto.getFileData()));
            return fileMetadata;

        } catch (Exception ex) {
            logger.error("[Exception][UploadFile] : ", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong, Can't upload file", ex);

        } finally {
            IOUtils.closeQuietly(bis);
        }
    }

    private String getContentTypeFromType(String type) {
        switch (type.toUpperCase()) {
            case "WORD":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "EXCEL":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "PDF":
                return "application/pdf";
            case "PNG":
                return "images/png";
            case "JPEG":
                return "images/jpeg";
            case "JPG":
                return "images/jpg";
        }

        throw new BadRequestException("Type: " + type + " ไม่่มีในระบบ / Type ที่สามารถเลือกได้ WORD, EXCEL, PDF, PNG, JPEG, JPG");
    }

    private void saveFile(int fileId, String filePath, String fileName) {
        com.example.shinsiri.entities.File file = fileRepository.findById(fileId).orElse(null);
        if (file == null) {
            throw new BadRequestException("fileId: " + fileId + " ไม่มีในระบบ");
        }

        file.setFilePath(filePath);
        file.setFileName(fileName);

        fileRepository.saveAndFlush(file);
    }

    private byte[] handleBase64ToByte(String base64) {
        String delis = "[,]";
        String[] parts = base64.split(delis);
        String imageStr = parts.length > 1 ? parts[1] : parts[0];
        return Base64.getDecoder().decode((imageStr));
    }

    private String handleBase64(String base64) {
        String delis = "[,]";
        String[] parts = base64.split(delis);
        return parts.length > 1 ? parts[1] : parts[0];
    }

    private boolean checkSizeIsToLargeFromByte(int maxSizeKB, byte[] fileBtye) {
        if ((fileBtye.length / 1024) > maxSizeKB) return true;
        return false;
    }

}
